package com.example.travelagent.domain;

public record ItineraryDay(
        int day,
        String theme,
        String morning,
        String afternoon,
        String evening,
        String transportTip
) {
}
