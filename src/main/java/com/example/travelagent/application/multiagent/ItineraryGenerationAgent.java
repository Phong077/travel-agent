package com.example.travelagent.application.multiagent;

import com.example.travelagent.application.AiResponseParseException;
import com.example.travelagent.application.TripPlanResponseValidator;
import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 行程生成 Agent。
 *
 * <p>职责：读取知识库检索、预算分析和天气分析结果，调用大模型生成最终结构化行程。
 * 它不直接负责查资料或算预算，只负责综合上下文完成“写行程”这一步。</p>
 */
@Component
public class ItineraryGenerationAgent implements TravelPlanningSubAgent {

    private static final Logger log = LoggerFactory.getLogger(ItineraryGenerationAgent.class);
    private static final int MAX_AI_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final TripPlanResponseValidator tripPlanResponseValidator;

    public ItineraryGenerationAgent(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            TripPlanResponseValidator tripPlanResponseValidator
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.tripPlanResponseValidator = tripPlanResponseValidator;
    }

    @Override
    public String name() {
        return "itinerary-generation-agent";
    }

    @Override
    public void execute(MultiAgentPlanningContext context) {
        String originalPrompt = buildPrompt(context);
        String currentPrompt = originalPrompt;
        AiResponseParseException lastException = null;

        for (int attempt = 1; attempt <= MAX_AI_ATTEMPTS; attempt++) {
            String rawResponse = chatClient.prompt()
                    .user(currentPrompt)
                    .call()
                    .content();

            log.debug("Raw multi-agent itinerary response, attempt={}: {}", attempt, rawResponse);

            try {
                TripPlanResponse response = parseTripPlan(rawResponse);
                tripPlanResponseValidator.validate(response, context.request());
                context.generatedPlan(response);
                context.generationAttempts(attempt);
                context.addToolCall(new ToolCallRecord(
                        name(),
                        "行程生成 Agent",
                        "已完成",
                        "已基于知识库、预算和天气上下文生成结构化行程，生成尝试次数：" + attempt + "。"
                ));
                return;
            } catch (AiResponseParseException exception) {
                lastException = exception;
                log.warn(
                        "Multi-agent itinerary validation failed, attempt={}/{}, reason={}",
                        attempt,
                        MAX_AI_ATTEMPTS,
                        exception.getMessage()
                );
                currentPrompt = buildRetryPrompt(originalPrompt, exception.getMessage());
            }
        }

        throw lastException == null
                ? new AiResponseParseException("多 Agent 行程生成失败。")
                : lastException;
    }

    private TripPlanResponse parseTripPlan(String content) {
        try {
            return objectMapper.readValue(cleanJson(content), TripPlanResponse.class);
        } catch (JsonProcessingException exception) {
            throw new AiResponseParseException("多 Agent 行程生成结果不是合法 JSON。", exception);
        }
    }

    private String cleanJson(String content) {
        if (content == null) {
            return "";
        }

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

    private String buildPrompt(MultiAgentPlanningContext context) {
        PlanTripRequest request = context.request();
        BudgetAnalysis budgetAnalysis = context.budgetAnalysis();
        WeatherInfo weatherInfo = context.weatherInfo();

        return """
                你是多 Agent 旅行规划系统中的“行程生成 Agent”。
                前面的子 Agent 已经完成了知识库检索、预算分析和天气分析。
                你的任务是综合这些上下文，生成一份适合前端展示的结构化中文旅行计划。

                输出要求：
                1. 必须只返回合法 JSON，不要返回 Markdown，不要使用代码块。
                2. 不要在 JSON 外输出任何解释文字。
                3. destination 必须严格等于用户填写的目的地：%s。
                4. totalDays 必须等于用户填写的旅行天数：%d。
                5. days 数组长度必须等于旅行天数。
                6. 每一天都必须包含 day、theme、morning、afternoon、evening、transportTip。
                7. JSON 中不要包含 references、budgetAnalysis、weatherInfo、toolCalls、generationMetadata 字段，这些字段由后端协调者补充。

                JSON 格式必须严格如下：
                {
                  "destination": "%s",
                  "totalDays": %d,
                  "summary": "整段行程的中文总结",
                  "days": [
                    {
                      "day": 1,
                      "theme": "当天中文主题",
                      "morning": "上午中文安排",
                      "afternoon": "下午中文安排",
                      "evening": "晚上中文安排",
                      "transportTip": "中文交通建议"
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

                知识库检索 Agent 提供的参考资料：
                %s

                预算分析 Agent 提供的约束：
                人均总预算：%d 元
                人均每日预算：%d 元
                预算等级：%s
                预算建议：%s

                天气风险 Agent 提供的约束：
                天气摘要：%s
                风险等级：%s
                天气建议：%s
                每日提醒：%s

                生成策略：
                1. 行程必须围绕用户目的地，不要跑到其他城市或省份。
                2. 优先使用知识库资料中的景点、美食、交通和季节信息。
                3. 如果预算偏紧，减少昂贵餐厅、长距离交通和频繁换酒店。
                4. 如果用户避免太早起床，不要安排早于 8:30 的出发。
                5. 如果用户避免每天换酒店，尽量用同一个住宿区域作为基地。
                6. 如果天气存在风险，每天安排中要体现雨具、防晒、保暖或室内备选建议。
                """.formatted(
                request.destination(),
                request.days(),
                request.destination(),
                request.days(),
                request.departureCity(),
                request.destination(),
                request.days(),
                request.travelers(),
                request.budget(),
                formatList(request.preferences()),
                formatList(request.avoid()),
                formatReferences(context.references()),
                budgetAnalysis.perPersonBudget(),
                budgetAnalysis.perPersonDailyBudget(),
                budgetAnalysis.level(),
                budgetAnalysis.suggestion(),
                weatherInfo.summary(),
                weatherInfo.riskLevel(),
                weatherInfo.suggestion(),
                formatList(weatherInfo.dailyTips())
        );
    }

    private String buildRetryPrompt(String originalPrompt, String failureReason) {
        return originalPrompt + """

                上一次生成结果没有通过后端校验，失败原因：%s
                请重新生成，并严格满足：
                1. destination 必须与用户目的地完全一致。
                2. totalDays 必须等于用户旅行天数。
                3. days 数组长度必须等于用户旅行天数。
                4. 每天的 day 序号必须从 1 开始连续递增。
                5. 每天的 theme、morning、afternoon、evening、transportTip 都不能为空。
                6. 只返回合法 JSON，不要输出任何解释文字。
                """.formatted(failureReason);
    }

    private String formatReferences(List<KnowledgeSearchResult> references) {
        if (references == null || references.isEmpty()) {
            return "没有命中知识库资料，请基于用户目的地谨慎生成。";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < references.size(); i++) {
            KnowledgeSearchResult reference = references.get(i);
            builder.append("[")
                    .append(i + 1)
                    .append("] ")
                    .append(reference.title())
                    .append("，来源：")
                    .append(reference.source())
                    .append("，匹配分数：")
                    .append(reference.score())
                    .append("\n")
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
}
