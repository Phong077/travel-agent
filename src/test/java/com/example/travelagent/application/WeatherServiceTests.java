package com.example.travelagent.application;

import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.WeatherInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherServiceTests {

    private final WeatherService weatherService = new WeatherService();

    @Test
    void shouldCreateWeatherTipsForYunnanTrip() {
        PlanTripRequest request = new PlanTripRequest(
                "重庆",
                "云南",
                4,
                2,
                6000,
                List.of("美食", "自然风景"),
                List.of("太早起床")
        );

        WeatherInfo weatherInfo = weatherService.analyze(request);

        assertThat(weatherInfo.destination()).isEqualTo("云南");
        assertThat(weatherInfo.riskLevel()).isEqualTo("昼夜温差");
        assertThat(weatherInfo.dailyTips()).hasSize(4);
        assertThat(weatherInfo.suggestion()).contains("太早起床");
    }
}
