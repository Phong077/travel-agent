package com.example.travelagent.application;

import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.WeatherInfo;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherServiceTests {

    private final WeatherService weatherService = new WeatherService(RestClient.create(), "", true);

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

    @Test
    void shouldUseAmapWeatherWhenApiKeyConfigured() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://restapi.amap.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        WeatherService service = new WeatherService(builder.build(), "test-key", true);

        server.expect(request -> assertThat(request.getURI().toString())
                        .contains("/v3/geocode/geo")
                        .contains("address=%E6%88%90%E9%83%BD"))
                .andRespond(withSuccess("""
                        {
                          "status": "1",
                          "geocodes": [
                            {
                              "adcode": "510100"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(request -> assertThat(request.getURI().toString())
                        .contains("/v3/weather/weatherInfo")
                        .contains("city=510100")
                        .contains("extensions=all"))
                .andRespond(withSuccess("""
                        {
                          "status": "1",
                          "forecasts": [
                            {
                              "city": "成都市",
                              "casts": [
                                {
                                  "date": "2026-07-17",
                                  "dayweather": "阵雨",
                                  "nightweather": "多云",
                                  "daytemp": "29",
                                  "nighttemp": "23"
                                }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherInfo weatherInfo = service.analyze(new PlanTripRequest(
                "重庆",
                "成都",
                2,
                2,
                5000,
                List.of("美食"),
                List.of("频繁换酒店")
        ));

        assertThat(weatherInfo.riskLevel()).isEqualTo("降雨影响");
        assertThat(weatherInfo.summary()).contains("高德天气预报").contains("成都市");
        assertThat(weatherInfo.dailyTips()).hasSize(2);
        assertThat(weatherInfo.suggestion()).contains("频繁换酒店");
        server.verify();
    }
}
