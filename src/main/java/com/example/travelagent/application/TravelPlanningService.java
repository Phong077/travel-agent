package com.example.travelagent.application;

import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.GenerationMetadata;
import com.example.travelagent.domain.KnowledgeReference;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.DestinationResolver;
import com.example.travelagent.knowledge.KnowledgeRetrievalService;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class TravelPlanningService {

    private static final Logger log = LoggerFactory.getLogger(TravelPlanningService.class);
    private static final int MAX_AI_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final BudgetService budgetService;
    private final DestinationResolver destinationResolver;
    private final WeatherService weatherService;
    private final TripPlanResponseValidator tripPlanResponseValidator;

    public TravelPlanningService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            KnowledgeRetrievalService knowledgeRetrievalService,
            BudgetService budgetService,
            DestinationResolver destinationResolver,
            WeatherService weatherService,
            TripPlanResponseValidator tripPlanResponseValidator
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.budgetService = budgetService;
        this.destinationResolver = destinationResolver;
        this.weatherService = weatherService;
        this.tripPlanResponseValidator = tripPlanResponseValidator;
    }

    public TripPlanResponse plan(PlanTripRequest request) {
        List<KnowledgeSearchResult> references = knowledgeRetrievalService.retrieve(request);
        BudgetAnalysis budgetAnalysis = budgetService.analyze(request);
        WeatherInfo weatherInfo = weatherService.analyze(request);
        String destinationKey = destinationResolver.resolve(request.destination());
        boolean hasDedicatedKnowledgeBase = destinationResolver.hasDedicatedKnowledgeBase(destinationKey);

        log.info(
                "Planning trip: destination={}, destinationKey={}, dedicatedKnowledgeBase={}, days={}, travelers={}, budget={}, references={}",
                request.destination(),
                destinationKey,
                hasDedicatedKnowledgeBase,
                request.days(),
                request.travelers(),
                request.budget(),
                references.size()
        );
        log.info(
                "Budget analysis: level={}, perPersonDailyBudget={}",
                budgetAnalysis.level(),
                budgetAnalysis.perPersonDailyBudget()
        );
        log.info(
                "Weather analysis: riskLevel={}, suggestion={}",
                weatherInfo.riskLevel(),
                weatherInfo.suggestion()
        );
        List<ToolCallRecord> toolCalls = buildServiceToolCalls(references, budgetAnalysis, weatherInfo);

        String prompt = buildPrompt(request, references, budgetAnalysis, weatherInfo, hasDedicatedKnowledgeBase);

        try {
            GenerationResult generationResult = generateValidatedTripPlan(prompt, request);
            return enrichResponse(generationResult, references, budgetAnalysis, weatherInfo, toolCalls);
        } catch (RuntimeException exception) {
            log.warn("AI generation failed, using backend fallback itinerary. destination={}", request.destination(), exception);
            return buildFallbackResponse(request, references, budgetAnalysis, weatherInfo, toolCalls);
        }
    }

    private GenerationResult generateValidatedTripPlan(String prompt, PlanTripRequest request) {
        String currentPrompt = prompt;
        AiResponseParseException lastException = null;

        for (int attempt = 1; attempt <= MAX_AI_ATTEMPTS; attempt++) {
            String json = chatClient.prompt()
                    .user(currentPrompt)
                    .call()
                    .content();

            log.debug("Raw AI response, attempt={}: {}", attempt, json);

            try {
                TripPlanResponse response = parseTripPlan(json);
                tripPlanResponseValidator.validate(response, request);
                return new GenerationResult(response, attempt);
            } catch (AiResponseParseException exception) {
                lastException = exception;
                log.warn(
                        "AI response validation failed, attempt={}/{}, reason={}",
                        attempt,
                        MAX_AI_ATTEMPTS,
                        exception.getMessage()
                );
                currentPrompt = buildRetryPrompt(prompt, exception.getMessage());
            }
        }

        throw lastException;
    }

    private TripPlanResponse enrichResponse(
            GenerationResult generationResult,
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis,
            WeatherInfo weatherInfo,
            List<ToolCallRecord> toolCalls
    ) {
        TripPlanResponse response = generationResult.response();

        return new TripPlanResponse(
                response.destination(),
                response.totalDays(),
                response.summary(),
                response.days(),
                toKnowledgeReferences(references),
                budgetAnalysis,
                weatherInfo,
                toolCalls,
                new GenerationMetadata("stable", generationResult.attempts(), true)
        );
    }

    private TripPlanResponse buildFallbackResponse(
            PlanTripRequest request,
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis,
            WeatherInfo weatherInfo,
            List<ToolCallRecord> toolCalls
    ) {
        List<ToolCallRecord> fallbackToolCalls = new ArrayList<>(toolCalls);
        fallbackToolCalls.add(new ToolCallRecord(
                "backend.fallback.itinerary",
                "后端规则兜底行程",
                "已启用",
                "大模型调用暂不可用时，由后端根据用户目的地、偏好、预算、天气和知识库引用生成兜底行程。"
        ));

        return new TripPlanResponse(
                request.destination(),
                request.days(),
                buildFallbackSummary(request, references, budgetAnalysis, weatherInfo),
                buildFallbackDays(request, references, weatherInfo),
                toKnowledgeReferences(references),
                budgetAnalysis,
                weatherInfo,
                fallbackToolCalls,
                new GenerationMetadata("backend-fallback", 0, false)
        );
    }

    private String buildFallbackSummary(
            PlanTripRequest request,
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis,
            WeatherInfo weatherInfo
    ) {
        String referenceText = references.isEmpty()
                ? "当前目的地暂无专属知识库，行程会结合通用旅行规则安排。"
                : "已参考 " + references.size() + " 条知识库资料。";

        return "这是后端规则兜底生成的“" + request.destination() + "”旅行计划。"
                + referenceText
                + "预算等级为" + budgetAnalysis.level()
                + "，天气风险为" + weatherInfo.riskLevel()
                + "，行程会尽量贴合偏好并避开用户限制。";
    }

    private List<com.example.travelagent.domain.ItineraryDay> buildFallbackDays(
            PlanTripRequest request,
            List<KnowledgeSearchResult> references,
            WeatherInfo weatherInfo
    ) {
        List<String> themes = List.of(
                "抵达与城市初印象",
                "核心景点与本地体验",
                "自然风光与轻松探索",
                "文化街区与深度漫游",
                "返程前的弹性安排"
        );
        List<com.example.travelagent.domain.ItineraryDay> days = new ArrayList<>();
        String preferenceText = formatList(request.preferences());
        String avoidText = formatList(request.avoid());

        for (int index = 0; index < request.days(); index++) {
            int day = index + 1;
            String theme = request.destination() + (index < themes.size() ? themes.get(index) : "第 " + day + " 天弹性探索");
            String referenceTitle = references.isEmpty()
                    ? request.destination() + "核心区域"
                    : references.get(Math.min(index, references.size() - 1)).title();
            String weatherTip = weatherInfo.dailyTips() == null || weatherInfo.dailyTips().isEmpty()
                    ? "出发前查看实时天气。"
                    : weatherInfo.dailyTips().get(Math.min(index, weatherInfo.dailyTips().size() - 1));

            days.add(new com.example.travelagent.domain.ItineraryDay(
                    day,
                    theme,
                    day == 1
                            ? "从" + request.departureCity() + "出发前往" + request.destination() + "，抵达后优先入住交通便利区域，保留休整时间。"
                            : "上午围绕“" + referenceTitle + "”安排轻量游览，避免过早出发和过度赶路。",
                    "下午结合“" + preferenceText + "”安排体验，优先选择交通顺路、节奏稳定的地点。"
                            + "天气提醒：" + weatherTip,
                    "晚上安排本地餐饮、城市漫步或轻松休息，注意避开“" + avoidText + "”。",
                    "这是后端规则兜底交通建议：优先选择公共交通、城际铁路和短距离步行，跨城或远距离景点需要预留交通缓冲。"
            ));
        }

        return days;
    }

    private List<ToolCallRecord> buildServiceToolCalls(
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis,
            WeatherInfo weatherInfo
    ) {
        int bestScore = references.stream()
                .mapToInt(KnowledgeSearchResult::score)
                .max()
                .orElse(0);

        return List.of(
                new ToolCallRecord(
                        "knowledgeRetrievalService.retrieve",
                        "知识库检索服务",
                        "已执行",
                        "命中 %d 条引用，最高相关度 %d".formatted(references.size(), bestScore)
                ),
                new ToolCallRecord(
                        "budgetService.analyze",
                        "预算分析服务",
                        "已执行",
                        "人均预算 %d 元，每日人均 %d 元，预算等级：%s".formatted(
                                budgetAnalysis.perPersonBudget(),
                                budgetAnalysis.perPersonDailyBudget(),
                                budgetAnalysis.level()
                        )
                ),
                new ToolCallRecord(
                        "weatherService.analyze",
                        "天气风险服务",
                        "已执行",
                        "%s：%s".formatted(weatherInfo.riskLevel(), weatherInfo.suggestion())
                )
        );
    }

    private String buildPrompt(
            PlanTripRequest request,
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis,
            WeatherInfo weatherInfo,
            boolean hasDedicatedKnowledgeBase
    ) {
        return """
                你是一名专业的旅行规划助手。
                请根据用户需求，生成一份真实、实用、节奏合理的旅行计划。
                本次用户填写的目的地是：%s。

                你必须只返回合法 JSON。
                不要返回 Markdown。
                不要使用代码块包裹结果。
                不要在 JSON 外输出任何解释文字。

                请优先参考下面的知识库资料。
                如果资料不足，可以结合常识补充，但不要编造不存在的景点、交通方式或价格。

                知识库参考资料：
                %s

                知识库状态：
                %s

                用户明确希望避免的内容：
                %s

                预算分析：
                人均总预算：%d 元
                人均每日预算：%d 元
                预算等级：%s
                预算建议：%s

                天气分析：
                天气摘要：%s
                风险等级：%s
                天气建议：%s

                如果避免项中提到“不早起”或“太早起床”，不要安排早于 8:30 的出发时间。
                如果避免项中提到“每天换酒店”或“频繁换酒店”，应尽量减少换酒店次数，优先选择交通便利的城市或区域作为基地。
                如果天气分析提示阵雨、强日照、高海拔或昼夜温差，需要在每日安排中加入室内备选、雨具、防晒或保暖提醒。

                规划要求：
                1. 必须围绕用户填写的目的地进行规划，不要擅自改成其他省份或城市。
                2. 每一天都要按照上午、下午、晚上进行安排。
                3. 行程节奏要合理，不要为了堆景点而安排过度赶路。
                4. 需要结合用户的预算、人数、偏好和避免项。
                5. 适当加入美食建议和交通建议。
                6. days 数组长度必须等于用户填写的旅行天数。
                7. summary、theme、morning、afternoon、evening、transportTip 都必须使用中文。
                8. JSON 中不要包含 references、budgetAnalysis、weatherInfo 和 toolCalls 字段，这些字段由后端补充。

                JSON 格式必须严格如下：
                {
                  "destination": "%s",
                  "totalDays": %d,
                  "summary": "这里填写整段行程的中文概述",
                  "days": [
                    {
                      "day": 1,
                      "theme": "这里填写当天中文主题",
                      "morning": "这里填写上午中文安排",
                      "afternoon": "这里填写下午中文安排",
                      "evening": "这里填写晚上中文安排",
                      "transportTip": "这里填写中文交通建议"
                    }
                  ]
                }

                用户需求：
                出发城市：%s
                目的地：%s
                旅行天数：%d
                出行人数：%d
                总预算：%d 元
                偏好：%s
                避免项：%s
                """.formatted(
                request.destination(),
                formatReferences(references),
                hasDedicatedKnowledgeBase
                        ? "已命中该目的地的专属知识库，请优先使用这些资料。"
                        : "当前目的地暂无专属知识库，仅命中通用旅行规则。请严格围绕用户填写的目的地，结合常识谨慎生成。",
                formatList(request.avoid()),
                budgetAnalysis.perPersonBudget(),
                budgetAnalysis.perPersonDailyBudget(),
                budgetAnalysis.level(),
                budgetAnalysis.suggestion(),
                weatherInfo.summary(),
                weatherInfo.riskLevel(),
                weatherInfo.suggestion(),
                request.destination(),
                request.days(),
                request.departureCity(),
                request.destination(),
                request.days(),
                request.travelers(),
                request.budget(),
                formatList(request.preferences()),
                formatList(request.avoid())
        );
    }

    private String buildRetryPrompt(String originalPrompt, String failureReason) {
        return originalPrompt + """

                上一次返回结果没有通过后端校验，失败原因：%s
                请重新生成，并严格满足：
                1. destination 必须与用户填写的目的地完全一致。
                2. totalDays 必须等于用户填写的旅行天数。
                3. days 数组长度必须等于用户填写的旅行天数。
                4. 每天的 day 序号必须从 1 开始连续递增。
                5. 每天的 theme、morning、afternoon、evening、transportTip 都不能为空。
                6. 只返回合法 JSON，不要输出任何解释文字。
                """.formatted(failureReason);
    }

    private String formatReferences(List<KnowledgeSearchResult> references) {
        if (references == null || references.isEmpty()) {
            return "没有匹配到知识库参考资料。";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < references.size(); i++) {
            KnowledgeSearchResult reference = references.get(i);
            builder.append("[")
                    .append(i + 1)
                    .append("] ")
                    .append(reference.title())
                    .append(" (")
                    .append(reference.source())
                    .append(")\n")
                    .append(reference.content())
                    .append("\n\n");
        }
        return builder.toString();
    }

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "无";
        }
        return String.join("、", values);
    }

    private TripPlanResponse parseTripPlan(String json) {
        try {
            return objectMapper.readValue(cleanJson(json), TripPlanResponse.class);
        } catch (JsonProcessingException e) {
            throw new AiResponseParseException("AI 返回的旅行计划不是合法 JSON。", e);
        }
    }

    private String cleanJson(String content) {
        String result = content.trim();

        if (result.startsWith("```json")) {
            result = result.substring(7);
        }
        if (result.startsWith("```")) {
            result = result.substring(3);
        }
        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - 3);
        }

        result = result.trim();

        int firstBraceIndex = result.indexOf('{');
        int lastBraceIndex = result.lastIndexOf('}');

        if (firstBraceIndex >= 0 && lastBraceIndex > firstBraceIndex) {
            result = result.substring(firstBraceIndex, lastBraceIndex + 1);
        }

        return result.trim();
    }

    private List<KnowledgeReference> toKnowledgeReferences(List<KnowledgeSearchResult> references) {
        return references.stream()
                .map(reference -> new KnowledgeReference(
                        reference.title(),
                        reference.source(),
                        toSnippet(reference.content()),
                        reference.score()
                ))
                .toList();
    }

    private String toSnippet(String content) {
        if (content == null || content.length() <= 80) {
            return content;
        }
        return content.substring(0, 80) + "...";
    }

    private record GenerationResult(
            TripPlanResponse response,
            int attempts
    ) {
    }
}
