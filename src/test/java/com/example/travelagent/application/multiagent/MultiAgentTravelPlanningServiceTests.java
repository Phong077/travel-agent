package com.example.travelagent.application.multiagent;

import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.ItineraryDay;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MultiAgentTravelPlanningServiceTests {

    private final KnowledgeRetrievalAgent knowledgeRetrievalAgent = mock(KnowledgeRetrievalAgent.class);
    private final BudgetAnalysisAgent budgetAnalysisAgent = mock(BudgetAnalysisAgent.class);
    private final WeatherAnalysisAgent weatherAnalysisAgent = mock(WeatherAnalysisAgent.class);
    private final ItineraryGenerationAgent itineraryGenerationAgent = mock(ItineraryGenerationAgent.class);

    private final MultiAgentTravelPlanningService service = new MultiAgentTravelPlanningService(
            knowledgeRetrievalAgent,
            budgetAnalysisAgent,
            weatherAnalysisAgent,
            itineraryGenerationAgent
    );

    @Test
    void shouldCoordinateAgentsAndReturnMultiAgentPlan() {
        PlanTripRequest request = new PlanTripRequest(
                "广州",
                "深圳",
                2,
                2,
                3000,
                List.of("美食", "城市漫步"),
                List.of("太早起床")
        );

        when(itineraryGenerationAgent.name()).thenReturn("itinerary-generation-agent");

        doAnswer(invocation -> {
            MultiAgentPlanningContext context = invocation.getArgument(0);
            context.references(List.of(
                    new KnowledgeSearchResult("深圳滨海", "guangdong/attractions.md", "深圳适合安排滨海公园和城市漫步。", 88)
            ));
            context.budgetAnalysis(new BudgetAnalysis(1500, 750, "适中", "预算充足"));
            context.weatherInfo(new WeatherInfo(
                    "深圳",
                    "天气适合出行",
                    "常规关注",
                    "注意防晒",
                    List.of("第 1 天注意防晒", "第 2 天关注阵雨")
            ));
            context.generatedPlan(new TripPlanResponse(
                    "深圳",
                    2,
                    "深圳两日旅行计划",
                    List.of(
                            new ItineraryDay(1, "滨海文艺初体验", "前往前海与海边公园", "安排城市漫步与美食体验", "在商圈附近用餐并散步", "优先使用地铁和短距离步行"),
                            new ItineraryDay(2, "创意街区与本地美食", "游览创意街区", "体验本地餐饮和城市文化", "预留返程时间", "跨区移动优先地铁")
                    ),
                    List.of(),
                    null,
                    null,
                    List.of(new ToolCallRecord("itinerary-generation-agent", "行程生成 Agent", "已完成", "已生成")),
                    null
            ));
            context.generationAttempts(1);
            context.addToolCall(new ToolCallRecord("knowledge-retrieval-agent", "知识库检索 Agent", "已完成", "命中 1 条知识库资料"));
            context.addToolCall(new ToolCallRecord("budget-analysis-agent", "预算分析 Agent", "已完成", "预算等级为适中"));
            context.addToolCall(new ToolCallRecord("weather-analysis-agent", "天气风险 Agent", "已完成", "常规关注"));
            context.addToolCall(new ToolCallRecord("itinerary-generation-agent", "行程生成 Agent", "已完成", "已生成结构化行程"));
            return null;
        }).when(itineraryGenerationAgent).execute(org.mockito.ArgumentMatchers.any());

        TripPlanResponse response = service.plan(request);

        assertThat(response.destination()).isEqualTo("深圳");
        assertThat(response.totalDays()).isEqualTo(2);
        assertThat(response.summary()).contains("深圳两日旅行计划");
        assertThat(response.references()).hasSize(1);
        assertThat(response.budgetAnalysis().level()).isEqualTo("适中");
        assertThat(response.weatherInfo().riskLevel()).isEqualTo("常规关注");
        assertThat(response.toolCalls()).hasSize(4);
        assertThat(response.generationMetadata().mode()).isEqualTo("multi-agent");
        assertThat(response.generationMetadata().attempts()).isEqualTo(1);
        assertThat(response.generationMetadata().validated()).isTrue();

        var ordered = inOrder(knowledgeRetrievalAgent, budgetAnalysisAgent, weatherAnalysisAgent, itineraryGenerationAgent);
        ordered.verify(knowledgeRetrievalAgent).execute(org.mockito.ArgumentMatchers.any());
        ordered.verify(budgetAnalysisAgent).execute(org.mockito.ArgumentMatchers.any());
        ordered.verify(weatherAnalysisAgent).execute(org.mockito.ArgumentMatchers.any());
        ordered.verify(itineraryGenerationAgent).execute(org.mockito.ArgumentMatchers.any());

        verifyNoMoreInteractions(knowledgeRetrievalAgent, budgetAnalysisAgent, weatherAnalysisAgent, itineraryGenerationAgent);
    }
}
