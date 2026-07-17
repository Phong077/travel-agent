package com.example.travelagent.api;

import com.example.travelagent.application.AgentTravelPlanningService;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.TripPlanResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/trips")
public class AgentTripController {

    private final AgentTravelPlanningService agentTravelPlanningService;

    public AgentTripController(AgentTravelPlanningService agentTravelPlanningService) {
        this.agentTravelPlanningService = agentTravelPlanningService;
    }

    @PostMapping("/plan")
    public ApiResponse<TripPlanResponse> plan(@Valid @RequestBody PlanTripRequest request) {
        return ApiResponse.success(agentTravelPlanningService.plan(request));
    }
}
