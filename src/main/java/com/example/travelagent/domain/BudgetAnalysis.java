package com.example.travelagent.domain;

public record BudgetAnalysis(
        int perPersonBudget,
        int perPersonDailyBudget,
        String level,
        String suggestion
) {
}
