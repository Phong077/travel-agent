package com.example.travelagent.application;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.example.travelagent.agent.ToolCallRecorder;
import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.KnowledgeRetrievalService;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReactAgentTravelPlanningServiceTests {

    private final ReactAgent reactAgent = mock(ReactAgent.class);
    private final ToolCallRecorder toolCallRecorder = mock(ToolCallRecorder.class);
    private final TripPlanResponseValidator tripPlanResponseValidator = mock(TripPlanResponseValidator.class);
    private final KnowledgeRetrievalService knowledgeRetrievalService = mock(KnowledgeRetrievalService.class);
    private final BudgetService budgetService = mock(BudgetService.class);
    private final WeatherService weatherService = mock(WeatherService.class);
    private final TravelPlanningService fallbackTravelPlanningService = mock(TravelPlanningService.class);
    private final AgentToolCallValidator agentToolCallValidator = mock(AgentToolCallValidator.class);

    private final ReactAgentTravelPlanningService service = new ReactAgentTravelPlanningService(
            reactAgent,
            new ObjectMapper(),
            toolCallRecorder,
            tripPlanResponseValidator,
            knowledgeRetrievalService,
            budgetService,
            weatherService,
            fallbackTravelPlanningService,
            agentToolCallValidator
    );

    @Test
    void shouldReturnReactAgentPlanWithToolCallsAndMetadata() throws Exception {
        PlanTripRequest request = new PlanTripRequest(
                "广州",
                "深圳",
                2,
                2,
                3000,
                List.of("美食", "城市漫步"),
                List.of("太早起床")
        );
        List<ToolCallRecord> toolCalls = List.of(
                toolCall("analyzeTravelBudget"),
                toolCall("analyzeTravelWeather"),
                toolCall("searchTravelKnowledge")
        );

        when(reactAgent.call(any(String.class))).thenReturn(new AssistantMessage("""
                {
                  "destination": "深圳",
                  "totalDays": 2,
                  "summary": "深圳两日旅行计划",
                  "days": [
                    {
                      "day": 1,
                      "theme": "滨海文艺初体验",
                      "morning": "前往前海与海边公园",
                      "afternoon": "安排城市漫步与美食体验",
                      "evening": "在商圈附近用餐并散步",
                      "transportTip": "优先使用地铁和短距离步行"
                    },
                    {
                      "day": 2,
                      "theme": "创意街区与本地美食",
                      "morning": "游览创意街区",
                      "afternoon": "体验本地餐饮和城市文化",
                      "evening": "预留返程时间",
                      "transportTip": "跨区移动优先地铁"
                    }
                  ]
                }
                """));
        when(toolCallRecorder.snapshot()).thenReturn(toolCalls);
        when(knowledgeRetrievalService.retrieve(request)).thenReturn(List.of(
                new KnowledgeSearchResult("深圳滨海", "guangdong/attractions.md", "深圳适合安排滨海公园和城市漫步。", 88)
        ));
        when(budgetService.analyze(request)).thenReturn(new BudgetAnalysis(1500, 750, "适中", "预算充足"));
        when(weatherService.analyze(request)).thenReturn(new WeatherInfo(
                "深圳",
                "天气适合出行",
                "常规关注",
                "注意防晒",
                List.of("第 1 天注意防晒", "第 2 天关注阵雨")
        ));

        TripPlanResponse response = service.plan(request);

        assertThat(response.destination()).isEqualTo("深圳");
        assertThat(response.totalDays()).isEqualTo(2);
        assertThat(response.days()).hasSize(2);
        assertThat(response.references()).hasSize(1);
        assertThat(response.toolCalls()).extracting(ToolCallRecord::name)
                .containsExactly("analyzeTravelBudget", "analyzeTravelWeather", "searchTravelKnowledge");
        assertThat(response.generationMetadata().mode()).isEqualTo("react-agent");
        assertThat(response.generationMetadata().attempts()).isEqualTo(1);
        assertThat(response.generationMetadata().validated()).isTrue();

        verify(tripPlanResponseValidator).validate(any(TripPlanResponse.class), any(PlanTripRequest.class));
        verify(agentToolCallValidator).validate(toolCalls);
    }

    private ToolCallRecord toolCall(String name) {
        return new ToolCallRecord(name, name, "已调用", "测试工具调用");
    }
}
