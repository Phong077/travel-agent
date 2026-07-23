package com.example.travelagent.application.multiagent;

import com.example.travelagent.application.BudgetService;
import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.ToolCallRecord;
import org.springframework.stereotype.Component;

/**
 * 预算分析 Agent。
 *
 * <p>职责：根据总预算、出行人数和旅行天数判断预算等级，
 * 给行程生成 Agent 提供“偏紧、适中、充裕”等预算约束。</p>
 */
@Component
public class BudgetAnalysisAgent implements TravelPlanningSubAgent {

    private final BudgetService budgetService;

    public BudgetAnalysisAgent(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Override
    public String name() {
        return "budget-analysis-agent";
    }

    @Override
    public void execute(MultiAgentPlanningContext context) {
        BudgetAnalysis analysis = budgetService.analyze(context.request());

        context.budgetAnalysis(analysis);
        context.addToolCall(new ToolCallRecord(
                name(),
                "预算分析 Agent",
                "已完成",
                "人均预算 %d 元，每日人均 %d 元，预算等级为%s。".formatted(
                        analysis.perPersonBudget(),
                        analysis.perPersonDailyBudget(),
                        analysis.level()
                )
        ));
    }
}
