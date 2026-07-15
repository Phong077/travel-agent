package com.example.travelagent.application;

import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.KnowledgeReference;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.knowledge.KnowledgeRetrievalService;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelPlanningService {

    private static final Logger log = LoggerFactory.getLogger(TravelPlanningService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final BudgetService budgetService;

    public TravelPlanningService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            KnowledgeRetrievalService knowledgeRetrievalService,
            BudgetService budgetService
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.budgetService = budgetService;
    }

    public TripPlanResponse plan(PlanTripRequest request) {
        List<KnowledgeSearchResult> references = knowledgeRetrievalService.retrieve(request);
        BudgetAnalysis budgetAnalysis = budgetService.analyze(request);

        log.info(
                "Planning trip: destination={}, days={}, travelers={}, budget={}, references={}",
                request.destination(),
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

        String prompt = buildPrompt(request, references, budgetAnalysis);

        String json = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.debug("Raw AI response: {}", json);

        TripPlanResponse response = parseTripPlan(json);
        return enrichResponse(response, references, budgetAnalysis);
    }

    private TripPlanResponse enrichResponse(
            TripPlanResponse response,
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis
    ) {
        return new TripPlanResponse(
                response.destination(),
                response.totalDays(),
                response.summary(),
                response.days(),
                toKnowledgeReferences(references),
                budgetAnalysis
        );
    }

    private String buildPrompt(
            PlanTripRequest request,
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis
    ) {
        return """
                你是一名专业的四川旅行规划助手。
                请根据用户需求，生成一份真实、实用、节奏合理的四川旅行计划。

                你必须只返回合法 JSON。
                不要返回 Markdown。
                不要使用代码块包裹结果。
                不要在 JSON 外输出任何解释文字。

                请优先参考下面的知识库资料。
                如果资料不足，可以结合常识补充，但不要编造不存在的景点、交通方式或价格。

                知识库参考资料：
                %s

                用户明确希望避免的内容：
                %s

                预算分析：
                人均总预算：%d 元
                人均每日预算：%d 元
                预算等级：%s
                预算建议：%s

                如果避免项中提到“不早起”或“太早起床”，不要安排早于 8:30 的出发时间。
                如果避免项中提到“每天换酒店”或“频繁换酒店”，应尽量减少换酒店次数，优先使用成都作为基地。

                规划要求：
                1. 优先考虑真实的四川目的地，例如成都、都江堰、青城山、乐山、峨眉山、九寨沟、黄龙、川西等。
                2. 每一天都要按照上午、下午、晚上进行安排。
                3. 行程节奏要合理，不要为了堆景点而安排过度赶路。
                4. 需要结合用户的预算、人数、偏好和避免项。
                5. 适当加入美食建议和交通建议。
                6. days 数组长度必须等于用户填写的旅行天数。
                7. summary、theme、morning、afternoon、evening、transportTip 都必须使用中文。
                8. JSON 中不要包含 references 和 budgetAnalysis 字段，这两个字段由后端补充。

                JSON 格式必须严格如下：
                {
                  "destination": "四川",
                  "totalDays": 5,
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
                formatReferences(references),
                formatList(request.avoid()),
                budgetAnalysis.perPersonBudget(),
                budgetAnalysis.perPersonDailyBudget(),
                budgetAnalysis.level(),
                budgetAnalysis.suggestion(),
                request.departureCity(),
                request.destination(),
                request.days(),
                request.travelers(),
                request.budget(),
                formatList(request.preferences()),
                formatList(request.avoid())
        );
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
}
