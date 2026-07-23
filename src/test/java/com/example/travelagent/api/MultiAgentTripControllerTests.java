package com.example.travelagent.api;

import com.example.travelagent.application.multiagent.MultiAgentTravelPlanningService;
import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.GenerationMetadata;
import com.example.travelagent.domain.ItineraryDay;
import com.example.travelagent.domain.KnowledgeReference;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(MultiAgentTripController.class)
class MultiAgentTripControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MultiAgentTravelPlanningService multiAgentTravelPlanningService;

    @Test
    void shouldReturnMultiAgentTripPlan() throws Exception {
        when(multiAgentTravelPlanningService.plan(any(PlanTripRequest.class))).thenReturn(new TripPlanResponse(
                "深圳",
                2,
                "深圳两日旅行计划",
                List.of(
                        new ItineraryDay(1, "滨海文艺初体验", "前往前海与海边公园", "安排城市漫步与美食体验", "在商圈附近用餐并散步", "优先使用地铁和短距离步行"),
                        new ItineraryDay(2, "创意街区与本地美食", "游览创意街区", "体验本地餐饮和城市文化", "预留返程时间", "跨区移动优先地铁")
                ),
                List.of(new KnowledgeReference("深圳滨海", "guangdong/attractions.md", "深圳适合安排滨海公园和城市漫步。", 88)),
                new BudgetAnalysis(1500, 750, "适中", "预算充足"),
                new WeatherInfo("深圳", "天气适合出行", "常规关注", "注意防晒", List.of("第 1 天注意防晒")),
                List.of(new ToolCallRecord("knowledge-retrieval-agent", "知识库检索 Agent", "已完成", "命中 1 条知识库资料")),
                new GenerationMetadata("multi-agent", 1, true)
        ));

        mockMvc.perform(post("/api/multi-agent/trips/plan")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "departureCity": "广州",
                                  "destination": "深圳",
                                  "days": 2,
                                  "travelers": 2,
                                  "budget": 3000,
                                  "preferences": ["美食", "城市漫步"],
                                  "avoid": ["太早起床"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.destination").value("深圳"))
                .andExpect(jsonPath("$.data.totalDays").value(2))
                .andExpect(jsonPath("$.data.generationMetadata.mode").value("multi-agent"))
                .andExpect(jsonPath("$.data.references[0].title").value("深圳滨海"));
    }
}
