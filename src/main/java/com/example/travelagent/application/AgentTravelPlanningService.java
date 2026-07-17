package com.example.travelagent.application;

import com.example.travelagent.agent.ToolCallRecorder;
import com.example.travelagent.agent.TravelAgentTools;
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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentTravelPlanningService {

    private static final Logger log = LoggerFactory.getLogger(AgentTravelPlanningService.class);
    private static final int MAX_AI_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final TravelAgentTools travelAgentTools;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final BudgetService budgetService;
    private final WeatherService weatherService;
    private final ToolCallRecorder toolCallRecorder;
    private final TripPlanResponseValidator tripPlanResponseValidator;
    private final AgentToolCallValidator agentToolCallValidator;

    public AgentTravelPlanningService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            TravelAgentTools travelAgentTools,
            KnowledgeRetrievalService knowledgeRetrievalService,
            BudgetService budgetService,
            WeatherService weatherService,
            ToolCallRecorder toolCallRecorder,
            TripPlanResponseValidator tripPlanResponseValidator,
            AgentToolCallValidator agentToolCallValidator
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.travelAgentTools = travelAgentTools;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.budgetService = budgetService;
        this.weatherService = weatherService;
        this.toolCallRecorder = toolCallRecorder;
        this.tripPlanResponseValidator = tripPlanResponseValidator;
        this.agentToolCallValidator = agentToolCallValidator;
    }

    public TripPlanResponse plan(PlanTripRequest request) {
        String prompt = buildAgentPrompt(request);

        AgentGenerationResult generationResult = generateValidatedTripPlan(prompt, request);
        return enrichResponse(generationResult, request);
    }

    private AgentGenerationResult generateValidatedTripPlan(String prompt, PlanTripRequest request) {
        String currentPrompt = prompt;
        AiResponseParseException lastException = null;

        for (int attempt = 1; attempt <= MAX_AI_ATTEMPTS; attempt++) {
            toolCallRecorder.clear();
            String json;
            List<ToolCallRecord> toolCalls;

            try {
                json = chatClient.prompt()
                        .tools(travelAgentTools)
                        .user(currentPrompt)
                        .call()
                        .content();
                toolCalls = toolCallRecorder.snapshot();
            } finally {
                toolCallRecorder.clear();
            }

            log.debug("Raw agent AI response, attempt={}: {}", attempt, json);

            try {
                TripPlanResponse response = parseTripPlan(json);
                tripPlanResponseValidator.validate(response, request);
                agentToolCallValidator.validate(toolCalls);
                return new AgentGenerationResult(response, toolCalls, attempt);
            } catch (AiResponseParseException exception) {
                lastException = exception;
                log.warn(
                        "Agent AI response validation failed, attempt={}/{}, reason={}",
                        attempt,
                        MAX_AI_ATTEMPTS,
                        exception.getMessage()
                );
                currentPrompt = buildRetryPrompt(prompt, exception.getMessage());
            }
        }

        throw lastException;
    }

    private String buildAgentPrompt(PlanTripRequest request) {
        return """
                你是一名支持工具调用的智能旅行规划 Agent。
                你需要先使用工具获取预算分析、天气分析和知识库检索结果，再生成最终旅行计划。

                工具调用要求：
                1. 必须调用 analyzeTravelBudget 工具分析预算。
                2. 必须调用 analyzeTravelWeather 工具分析天气风险。
                3. 必须调用 searchTravelKnowledge 工具检索本地旅行知识库。
                4. 最终结果要综合工具返回的信息生成。

                最终回答要求：
                你必须只返回合法 JSON。
                不要返回 Markdown。
                不要使用代码块包裹结果。
                不要在 JSON 外输出任何解释文字。
                JSON 中不要包含 references、budgetAnalysis、weatherInfo 和 toolCalls 字段，这些字段由后端补充。

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
                请重新生成最终 JSON。工具仍然需要按要求调用，最终 JSON 必须满足：
                1. destination 必须与用户填写的目的地完全一致。
                2. totalDays 必须等于用户填写的旅行天数。
                3. days 数组长度必须等于用户填写的旅行天数。
                4. 每天的 day 序号必须从 1 开始连续递增。
                5. 每天的 theme、morning、afternoon、evening、transportTip 都不能为空。
                6. 必须调用 analyzeTravelBudget、analyzeTravelWeather、searchTravelKnowledge 三个工具。
                7. 只返回合法 JSON，不要输出任何解释文字。
                """.formatted(failureReason);
    }

    private TripPlanResponse enrichResponse(
            AgentGenerationResult generationResult,
            PlanTripRequest request
    ) {
        TripPlanResponse response = generationResult.response();
        List<KnowledgeSearchResult> references = knowledgeRetrievalService.retrieve(request);
        BudgetAnalysis budgetAnalysis = budgetService.analyze(request);
        WeatherInfo weatherInfo = weatherService.analyze(request);

        return new TripPlanResponse(
                response.destination(),
                response.totalDays(),
                response.summary(),
                response.days(),
                toKnowledgeReferences(references),
                budgetAnalysis,
                weatherInfo,
                generationResult.toolCalls(),
                new GenerationMetadata("agent", generationResult.attempts(), true)
        );
    }

    private TripPlanResponse parseTripPlan(String json) {
        try {
            return objectMapper.readValue(cleanJson(json), TripPlanResponse.class);
        } catch (JsonProcessingException e) {
            throw new AiResponseParseException("AI Agent 返回的旅行计划不是合法 JSON。", e);
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

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "无";
        }
        return String.join("、", values);
    }

    private record AgentGenerationResult(
            TripPlanResponse response,
            List<ToolCallRecord> toolCalls,
            int attempts
    ) {
    }
}
