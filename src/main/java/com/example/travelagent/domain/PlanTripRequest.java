package com.example.travelagent.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PlanTripRequest(
        @NotBlank(message = "departureCity is required")
        String departureCity,

        @NotBlank(message = "destination is required")
        String destination,

        @Min(value = 1, message = "days must be at least 1")
        int days,

        @Min(value = 1, message = "travelers must be at least 1")
        int travelers,

        @Min(value = 0, message = "budget cannot be negative")
        int budget,

        List<String> preferences,

        List<String> avoid
) {
}
