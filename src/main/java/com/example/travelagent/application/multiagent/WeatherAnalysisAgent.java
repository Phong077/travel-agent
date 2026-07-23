package com.example.travelagent.application.multiagent;

import com.example.travelagent.application.WeatherService;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.WeatherInfo;
import org.springframework.stereotype.Component;

/**
 * 天气风险 Agent。
 *
 * <p>职责：根据目的地和天数分析天气风险，
 * 为后续行程生成提供雨天备选、防晒、保暖、户外时段调整等约束。</p>
 */
@Component
public class WeatherAnalysisAgent implements TravelPlanningSubAgent {

    private final WeatherService weatherService;

    public WeatherAnalysisAgent(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public String name() {
        return "weather-analysis-agent";
    }

    @Override
    public void execute(MultiAgentPlanningContext context) {
        WeatherInfo weatherInfo = weatherService.analyze(context.request());

        context.weatherInfo(weatherInfo);
        context.addToolCall(new ToolCallRecord(
                name(),
                "天气风险 Agent",
                "已完成",
                "%s：%s".formatted(weatherInfo.riskLevel(), weatherInfo.suggestion())
        ));
    }
}
