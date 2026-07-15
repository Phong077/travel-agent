package com.example.travelagent.api;

import com.example.travelagent.application.TravelPlanningService;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.TripPlanResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TravelPlanningService travelPlanningService;

    public TripController(TravelPlanningService travelPlanningService) {
        this.travelPlanningService = travelPlanningService;
    }

    @PostMapping("/plan")
    public ApiResponse<TripPlanResponse> plan(@Valid @RequestBody PlanTripRequest request) {
        return ApiResponse.success(travelPlanningService.plan(request));
    }
}
