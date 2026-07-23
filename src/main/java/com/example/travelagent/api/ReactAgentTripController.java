package com.example.travelagent.api;

import com.example.travelagent.application.ReactAgentTravelPlanningService;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.TripPlanResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/react-agent/trips")
public class ReactAgentTripController {

    private final ReactAgentTravelPlanningService reactAgentTravelPlanningService;

    public ReactAgentTripController(ReactAgentTravelPlanningService reactAgentTravelPlanningService) {
        this.reactAgentTravelPlanningService = reactAgentTravelPlanningService;
    }

    @PostMapping("/plan")
    public ApiResponse<TripPlanResponse> plan(@Valid @RequestBody PlanTripRequest request) {
        return ApiResponse.success(reactAgentTravelPlanningService.plan(request));
    }
}
