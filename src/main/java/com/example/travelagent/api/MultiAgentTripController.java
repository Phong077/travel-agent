package com.example.travelagent.api;

import com.example.travelagent.application.multiagent.MultiAgentTravelPlanningService;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.TripPlanResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多 Agent 旅行规划接口。
 *
 * <p>这个接口对外暴露“多 Agent 协同版”生成能力：
 * 先调度知识库、预算和天气子 Agent，
 * 再由行程生成 Agent 综合这些结果输出最终计划。</p>
 */
@RestController
@RequestMapping("/api/multi-agent/trips")
public class MultiAgentTripController {

    private final MultiAgentTravelPlanningService multiAgentTravelPlanningService;

    public MultiAgentTripController(MultiAgentTravelPlanningService multiAgentTravelPlanningService) {
        this.multiAgentTravelPlanningService = multiAgentTravelPlanningService;
    }

    @PostMapping("/plan")
    public ApiResponse<TripPlanResponse> plan(@Valid @RequestBody PlanTripRequest request) {
        return ApiResponse.success(multiAgentTravelPlanningService.plan(request));
    }
}
