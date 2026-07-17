package com.example.travelagent.application;

import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.WeatherInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final RestClient restClient;
    private final String amapApiKey;
    private final boolean amapWeatherEnabled;

    public WeatherService(
            @Value("${amap.api-key:}") String amapApiKey,
            @Value("${amap.weather.enabled:true}") boolean amapWeatherEnabled
    ) {
        this(RestClient.builder().baseUrl("https://restapi.amap.com").build(), amapApiKey, amapWeatherEnabled);
    }

    WeatherService(RestClient restClient, String amapApiKey, boolean amapWeatherEnabled) {
        this.restClient = restClient;
        this.amapApiKey = amapApiKey;
        this.amapWeatherEnabled = amapWeatherEnabled;
    }

    public WeatherInfo analyze(PlanTripRequest request) {
        if (amapWeatherEnabled && amapApiKey != null && !amapApiKey.isBlank()) {
            Optional<WeatherInfo> realWeatherInfo = queryAmapWeather(request);
            if (realWeatherInfo.isPresent()) {
                return realWeatherInfo.get();
            }
        }

        return analyzeByRules(request);
    }

    private Optional<WeatherInfo> queryAmapWeather(PlanTripRequest request) {
        try {
            Optional<String> adcode = queryAdcode(request.destination());
            if (adcode.isEmpty()) {
                log.warn("Amap geocode returned empty adcode for destination={}", request.destination());
                return Optional.empty();
            }

            AmapWeatherResponse weatherResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v3/weather/weatherInfo")
                            .queryParam("key", amapApiKey)
                            .queryParam("city", adcode.get())
                            .queryParam("extensions", "all")
                            .queryParam("output", "JSON")
                            .build())
                    .retrieve()
                    .body(AmapWeatherResponse.class);

            return toWeatherInfo(request, weatherResponse);
        } catch (RuntimeException exception) {
            log.warn("Amap weather query failed, fallback to rule-based weather. destination={}", request.destination());
            return Optional.empty();
        }
    }

    private Optional<String> queryAdcode(String destination) {
        AmapGeoResponse geoResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v3/geocode/geo")
                        .queryParam("key", amapApiKey)
                        .queryParam("address", destination)
                        .queryParam("output", "JSON")
                        .build())
                .retrieve()
                .body(AmapGeoResponse.class);

        if (geoResponse == null || !"1".equals(geoResponse.status()) || geoResponse.geocodes() == null) {
            return Optional.empty();
        }

        return geoResponse.geocodes().stream()
                .map(AmapGeocode::adcode)
                .filter(adcode -> adcode != null && !adcode.isBlank())
                .findFirst();
    }

    private Optional<WeatherInfo> toWeatherInfo(PlanTripRequest request, AmapWeatherResponse response) {
        if (response == null || !"1".equals(response.status()) || response.forecasts() == null || response.forecasts().isEmpty()) {
            return Optional.empty();
        }

        AmapForecast forecast = response.forecasts().get(0);
        List<AmapCast> casts = forecast.casts() == null ? List.of() : forecast.casts();
        if (casts.isEmpty()) {
            return Optional.empty();
        }

        String riskLevel = resolveForecastRiskLevel(casts);
        String city = firstNonBlank(forecast.city(), request.destination());
        String summary = "高德天气预报：" + city + "未来 " + casts.size() + " 天需要重点关注：" + riskLevel + "。";
        String suggestion = buildAmapSuggestion(request.destination(), riskLevel, casts, request.avoid());
        List<String> dailyTips = buildAmapDailyTips(request.days(), casts, riskLevel);

        return Optional.of(new WeatherInfo(request.destination(), summary, riskLevel, suggestion, dailyTips));
    }

    private WeatherInfo analyzeByRules(PlanTripRequest request) {
        String destination = request.destination();
        String riskLevel = resolveRiskLevel(destination);
        String summary = buildSummary(destination, riskLevel);
        String suggestion = buildSuggestion(destination, riskLevel, request.avoid());
        List<String> dailyTips = buildDailyTips(request.days(), riskLevel);

        return new WeatherInfo(destination, summary, riskLevel, suggestion, dailyTips);
    }

    private String resolveForecastRiskLevel(List<AmapCast> casts) {
        boolean hasRain = casts.stream().anyMatch(cast -> containsAny(cast.dayweather(), "雨", "雷"));
        boolean hasSnow = casts.stream().anyMatch(cast -> containsAny(cast.dayweather(), "雪"));
        boolean hasHotDay = casts.stream()
                .map(AmapCast::daytemp)
                .map(this::parseInteger)
                .filter(Objects::nonNull)
                .anyMatch(temp -> temp >= 30);
        boolean hasLargeTemperatureGap = casts.stream().anyMatch(cast -> {
            Integer dayTemp = parseInteger(cast.daytemp());
            Integer nightTemp = parseInteger(cast.nighttemp());
            return dayTemp != null && nightTemp != null && dayTemp - nightTemp >= 10;
        });

        if (hasSnow) {
            return "雨雪低温";
        }
        if (hasRain) {
            return "降雨影响";
        }
        if (hasHotDay) {
            return "高温防晒";
        }
        if (hasLargeTemperatureGap) {
            return "昼夜温差";
        }
        return "天气平稳";
    }

    private String buildAmapSuggestion(String destination, String riskLevel, List<AmapCast> casts, List<String> avoid) {
        String avoidText = avoid == null || avoid.isEmpty() ? "过度奔波" : String.join("、", avoid);
        String firstDayWeather = casts.get(0).dayweather();
        return "规划“" + destination + "”行程时，建议结合未来天气调整户外景点顺序。首日白天天气为"
                + firstNonBlank(firstDayWeather, "未知")
                + "，整体风险为"
                + riskLevel
                + "，建议避免"
                + avoidText
                + "。";
    }

    private List<String> buildAmapDailyTips(int days, List<AmapCast> casts, String riskLevel) {
        List<String> tips = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            AmapCast cast = casts.get(Math.min(day - 1, casts.size() - 1));
            tips.add("第 " + day + " 天：" + firstNonBlank(cast.date(), "出行当日")
                    + "白天" + firstNonBlank(cast.dayweather(), "天气待确认")
                    + "，夜间" + firstNonBlank(cast.nightweather(), "天气待确认")
                    + "，气温约 " + firstNonBlank(cast.nighttemp(), "-")
                    + "-" + firstNonBlank(cast.daytemp(), "-")
                    + "℃，关注" + riskLevel + "。");
        }
        return tips;
    }

    private String resolveRiskLevel(String destination) {
        if (containsAny(destination, "云南", "昆明", "大理", "丽江", "香格里拉")) {
            return "昼夜温差";
        }
        if (containsAny(destination, "四川", "成都", "川西", "九寨沟", "峨眉山")) {
            return "多云阵雨";
        }
        if (containsAny(destination, "新疆", "西藏", "青海")) {
            return "高海拔与强日照";
        }
        return "常规关注";
    }

    private String buildSummary(String destination, String riskLevel) {
        return "当前为规则版天气分析，目的地“" + destination + "”需要重点关注：" + riskLevel + "。";
    }

    private String buildSuggestion(String destination, String riskLevel, List<String> avoid) {
        String avoidText = avoid == null || avoid.isEmpty() ? "过度奔波" : String.join("、", avoid);
        return "规划“" + destination + "”行程时，建议预留室内备选方案，随身携带雨具、防晒或外套，并避免" + avoidText + "。";
    }

    private List<String> buildDailyTips(int days, String riskLevel) {
        List<String> tips = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            tips.add("第 " + day + " 天关注" + riskLevel + "，户外活动建议安排在天气更稳定的时段。");
        }
        return tips;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String firstNonBlank(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first;
    }

    private record AmapGeoResponse(
            String status,
            List<AmapGeocode> geocodes
    ) {
    }

    private record AmapGeocode(
            String adcode
    ) {
    }

    private record AmapWeatherResponse(
            String status,
            List<AmapForecast> forecasts
    ) {
    }

    private record AmapForecast(
            String city,
            List<AmapCast> casts
    ) {
    }

    private record AmapCast(
            String date,
            String dayweather,
            String nightweather,
            String daytemp,
            String nighttemp
    ) {
    }
}
