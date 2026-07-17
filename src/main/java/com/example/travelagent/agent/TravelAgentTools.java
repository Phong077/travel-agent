package com.example.travelagent.agent;

import com.example.travelagent.application.BudgetService;
import com.example.travelagent.application.WeatherService;
import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.KnowledgeRetrievalService;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TravelAgentTools {

    private static final Logger log = LoggerFactory.getLogger(TravelAgentTools.class);

    private final BudgetService budgetService;
    private final WeatherService weatherService;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final ToolCallRecorder toolCallRecorder;

    public TravelAgentTools(
            BudgetService budgetService,
            WeatherService weatherService,
            KnowledgeRetrievalService knowledgeRetrievalService,
            ToolCallRecorder toolCallRecorder
    ) {
        this.budgetService = budgetService;
        this.weatherService = weatherService;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.toolCallRecorder = toolCallRecorder;
    }

    @Tool(description = "根据旅行天数、出行人数和总预算，计算人均预算、每日人均预算、预算等级和预算建议。")
    public BudgetAnalysis analyzeTravelBudget(
            @ToolParam(description = "出发城市") String departureCity,
            @ToolParam(description = "目的地") String destination,
            @ToolParam(description = "旅行天数") int days,
            @ToolParam(description = "出行人数") int travelers,
            @ToolParam(description = "总预算，单位元") int budget,
            @ToolParam(description = "旅行偏好") List<String> preferences,
            @ToolParam(description = "需要避免的事项") List<String> avoid
    ) {
        log.info("Agent tool called: analyzeTravelBudget, destination={}, days={}, travelers={}, budget={}",
                destination, days, travelers, budget);
        BudgetAnalysis analysis = budgetService.analyze(toRequest(departureCity, destination, days, travelers, budget, preferences, avoid));
        toolCallRecorder.record(
                "analyzeTravelBudget",
                "预算分析工具",
                "已调用",
                "人均预算 %d 元，每日人均 %d 元，预算等级：%s".formatted(
                        analysis.perPersonBudget(),
                        analysis.perPersonDailyBudget(),
                        analysis.level()
                )
        );
        return analysis;
    }

    @Tool(description = "根据目的地和旅行天数，生成天气风险等级、天气摘要、天气建议和每日天气提醒。")
    public WeatherInfo analyzeTravelWeather(
            @ToolParam(description = "出发城市") String departureCity,
            @ToolParam(description = "目的地") String destination,
            @ToolParam(description = "旅行天数") int days,
            @ToolParam(description = "出行人数") int travelers,
            @ToolParam(description = "总预算，单位元") int budget,
            @ToolParam(description = "旅行偏好") List<String> preferences,
            @ToolParam(description = "需要避免的事项") List<String> avoid
    ) {
        log.info("Agent tool called: analyzeTravelWeather, destination={}, days={}", destination, days);
        WeatherInfo weatherInfo = weatherService.analyze(toRequest(departureCity, destination, days, travelers, budget, preferences, avoid));
        toolCallRecorder.record(
                "analyzeTravelWeather",
                "天气风险工具",
                "已调用",
                "%s：%s".formatted(weatherInfo.riskLevel(), weatherInfo.suggestion())
        );
        return weatherInfo;
    }

    @Tool(description = "根据目的地、偏好和避坑项检索本地旅行知识库，返回可用于生成行程的参考资料。")
    public List<KnowledgeSearchResult> searchTravelKnowledge(
            @ToolParam(description = "出发城市") String departureCity,
            @ToolParam(description = "目的地") String destination,
            @ToolParam(description = "旅行天数") int days,
            @ToolParam(description = "出行人数") int travelers,
            @ToolParam(description = "总预算，单位元") int budget,
            @ToolParam(description = "旅行偏好") List<String> preferences,
            @ToolParam(description = "需要避免的事项") List<String> avoid
    ) {
        log.info("Agent tool called: searchTravelKnowledge, destination={}, preferences={}, avoid={}",
                destination, preferences == null ? List.of() : preferences, avoid == null ? List.of() : avoid);
        List<KnowledgeSearchResult> results = knowledgeRetrievalService.retrieve(toRequest(departureCity, destination, days, travelers, budget, preferences, avoid));
        int bestScore = results.stream()
                .mapToInt(KnowledgeSearchResult::score)
                .max()
                .orElse(0);
        toolCallRecorder.record(
                "searchTravelKnowledge",
                "知识库检索工具",
                "已调用",
                "命中 %d 条引用，最高相关度 %d".formatted(results.size(), bestScore)
        );
        return results;
    }

    private PlanTripRequest toRequest(
            String departureCity,
            String destination,
            int days,
            int travelers,
            int budget,
            List<String> preferences,
            List<String> avoid
    ) {
        return new PlanTripRequest(
                departureCity,
                destination,
                days,
                travelers,
                budget,
                preferences == null ? List.of() : preferences,
                avoid == null ? List.of() : avoid
        );
    }
}
