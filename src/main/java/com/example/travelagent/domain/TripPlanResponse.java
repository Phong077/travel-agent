package com.example.travelagent.domain;

import java.util.List;

public record TripPlanResponse(
        String destination,
        int totalDays,
        String summary,
        List<ItineraryDay> days,
        List<KnowledgeReference> references,
        BudgetAnalysis budgetAnalysis,
        WeatherInfo weatherInfo,
        List<ToolCallRecord> toolCalls,
        GenerationMetadata generationMetadata
) {
}
