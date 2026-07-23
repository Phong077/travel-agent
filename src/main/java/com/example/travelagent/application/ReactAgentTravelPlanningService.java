package com.example.travelagent.application;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.example.travelagent.agent.ToolCallRecorder;
import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.GenerationMetadata;
import com.example.travelagent.domain.KnowledgeReference;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.KnowledgeRetrievalService;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReactAgentTravelPlanningService {

    private static final Logger log = LoggerFactory.getLogger(ReactAgentTravelPlanningService.class);
    private static final int MAX_ATTEMPTS = 2;

    private final ReactAgent travelReactAgent;
    private final ObjectMapper objectMapper;
    private final ToolCallRecorder toolCallRecorder;
    private final TripPlanResponseValidator tripPlanResponseValidator;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final BudgetService budgetService;
    private final WeatherService weatherService;
    private final TravelPlanningService fallbackTravelPlanningService;
    private final AgentToolCallValidator agentToolCallValidator;

    public ReactAgentTravelPlanningService(
            ReactAgent travelReactAgent,
            ObjectMapper objectMapper,
            ToolCallRecorder toolCallRecorder,
            TripPlanResponseValidator tripPlanResponseValidator,
            KnowledgeRetrievalService knowledgeRetrievalService,
            BudgetService budgetService,
            WeatherService weatherService,
            TravelPlanningService fallbackTravelPlanningService,
            AgentToolCallValidator agentToolCallValidator
    ) {
        this.travelReactAgent = travelReactAgent;
        this.objectMapper = objectMapper;
        this.toolCallRecorder = toolCallRecorder;
        this.tripPlanResponseValidator = tripPlanResponseValidator;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.budgetService = budgetService;
        this.weatherService = weatherService;
        this.fallbackTravelPlanningService = fallbackTravelPlanningService;
        this.agentToolCallValidator = agentToolCallValidator;
    }

    public TripPlanResponse plan(PlanTripRequest request) {
        String originalPrompt = buildPrompt(request);
        AiResponseParseException lastValidationException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            toolCallRecorder.clear();

            try {
                String prompt = attempt == 1
                        ? originalPrompt
                        : buildRetryPrompt(originalPrompt, lastValidationException.getMessage());
                AssistantMessage assistantMessage = travelReactAgent.call(prompt);
                String rawResponse = assistantMessage.getText();
                List<ToolCallRecord> toolCalls = toolCallRecorder.snapshot();

                log.debug("Raw ReactAgent response, attempt={}: {}", attempt, rawResponse);

                TripPlanResponse response = parseTripPlan(rawResponse);
                tripPlanResponseValidator.validate(response, request);
                agentToolCallValidator.validate(toolCalls);
                return enrichResponse(response, request, toolCalls, attempt);
            } catch (AiResponseParseException exception) {
                lastValidationException = exception;
                log.warn("ReactAgent response validation failed, attempt={}/{}, reason={}",
                        attempt, MAX_ATTEMPTS, exception.getMessage());
            } catch (Exception exception) {
                log.warn("ReactAgent generation failed, using stable planning fallback. destination={}",
                        request.destination(), exception);
                return fallbackTravelPlanningService.plan(request);
            } finally {
                toolCallRecorder.clear();
            }
        }

        log.warn("ReactAgent reached max validation attempts, using stable planning fallback. destination={}",
                request.destination());
        return fallbackTravelPlanningService.plan(request);
    }

    private TripPlanResponse enrichResponse(
            TripPlanResponse response,
            PlanTripRequest request,
            List<ToolCallRecord> toolCalls,
            int attempts
    ) {
        List<KnowledgeSearchResult> references = knowledgeRetrievalService.retrieve(request);
        BudgetAnalysis budgetAnalysis = budgetService.analyze(request);
        WeatherInfo weatherInfo = weatherService.analyze(request);
        List<ToolCallRecord> enrichedToolCalls = new ArrayList<>(toolCalls);

        if (enrichedToolCalls.isEmpty()) {
            enrichedToolCalls.add(new ToolCallRecord(
                    "reactAgent.call",
                    "ReactAgent 规划",
                    "已调用",
                    "ReactAgent 已完成行程生成，但当前未捕获到工具调用记录。"
            ));
        }

        return new TripPlanResponse(
                response.destination(),
                response.totalDays(),
                response.summary(),
                response.days(),
                toKnowledgeReferences(references),
                budgetAnalysis,
                weatherInfo,
                enrichedToolCalls,
                new GenerationMetadata("react-agent", attempts, true)
        );
    }

    private TripPlanResponse parseTripPlan(String json) {
        try {
            return objectMapper.readValue(cleanJson(json), TripPlanResponse.class);
        } catch (JsonProcessingException exception) {
            throw new AiResponseParseException("ReactAgent 返回的旅行计划不是合法 JSON。", exception);
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

    private String buildPrompt(PlanTripRequest request) {
        return """
                请为下面的用户需求生成一份结构化旅行计划。

                你需要按照 ReAct 思路完成任务：
                1. 先理解用户的出发地、目的地、天数、人数、预算、偏好和避坑项。
                2. 必须调用预算分析、天气分析和知识库检索工具。
                3. 最后综合工具结果，生成可直接展示给前端的旅行计划。

                必须调用的工具：
                1. analyzeTravelBudget：分析预算等级和预算建议。
                2. analyzeTravelWeather：分析天气风险和每日天气提醒。
                3. searchTravelKnowledge：检索目的地知识库资料。

                最终只返回合法 JSON，不要返回 Markdown，不要使用代码块，不要在 JSON 外输出解释。
                JSON 中不要包含 references、budgetAnalysis、weatherInfo、toolCalls、generationMetadata 字段，这些字段由后端补充。

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
                避坑项：%s
                """.formatted(
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

                上一次 ReactAgent 返回结果没有通过后端校验，失败原因：%s
                请重新生成最终 JSON，并确保：
                1. destination 必须与用户目的地完全一致。
                2. totalDays 必须等于用户旅行天数。
                3. days 数组长度必须等于用户旅行天数。
                4. 每天的 day 序号必须从 1 开始连续递增。
                5. 每天的 theme、morning、afternoon、evening、transportTip 都不能为空。
                6. 必须重新调用 analyzeTravelBudget、analyzeTravelWeather、searchTravelKnowledge 三个工具。
                7. 只返回合法 JSON，不要输出任何解释文字。
                """.formatted(failureReason);
    }

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "无";
        }
        return String.join("、", values);
    }
}
