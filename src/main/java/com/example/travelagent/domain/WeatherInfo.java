package com.example.travelagent.domain;

import java.util.List;

public record WeatherInfo(
        String destination,
        String summary,
        String riskLevel,
        String suggestion,
        List<String> dailyTips
) {
}
