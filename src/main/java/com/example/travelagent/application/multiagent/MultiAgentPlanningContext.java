package com.example.travelagent.application.multiagent;

import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.KnowledgeSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 多 Agent 协同时共享的上下文。
 *
 * <p>可以把它理解成一张“旅行规划任务单”：用户原始需求先放进来，
 * 后续每个子 Agent 只负责补充自己擅长的部分，最后由协调者组装结果。</p>
 */
public class MultiAgentPlanningContext {

    private final PlanTripRequest request;
    private final List<ToolCallRecord> toolCalls = new ArrayList<>();

    private List<KnowledgeSearchResult> references = List.of();
    private BudgetAnalysis budgetAnalysis;
    private WeatherInfo weatherInfo;
    private TripPlanResponse generatedPlan;
    private int generationAttempts;

    public MultiAgentPlanningContext(PlanTripRequest request) {
        this.request = request;
    }

    public PlanTripRequest request() {
        return request;
    }

    public List<KnowledgeSearchResult> references() {
        return references;
    }

    public void references(List<KnowledgeSearchResult> references) {
        this.references = references == null ? List.of() : references;
    }

    public BudgetAnalysis budgetAnalysis() {
        return budgetAnalysis;
    }

    public void budgetAnalysis(BudgetAnalysis budgetAnalysis) {
        this.budgetAnalysis = budgetAnalysis;
    }

    public WeatherInfo weatherInfo() {
        return weatherInfo;
    }

    public void weatherInfo(WeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

    public TripPlanResponse generatedPlan() {
        return generatedPlan;
    }

    public void generatedPlan(TripPlanResponse generatedPlan) {
        this.generatedPlan = generatedPlan;
    }

    public int generationAttempts() {
        return generationAttempts;
    }

    public void generationAttempts(int generationAttempts) {
        this.generationAttempts = generationAttempts;
    }

    public List<ToolCallRecord> toolCalls() {
        return List.copyOf(toolCalls);
    }

    public void addToolCall(ToolCallRecord toolCall) {
        if (toolCall != null) {
            toolCalls.add(toolCall);
        }
    }
}
