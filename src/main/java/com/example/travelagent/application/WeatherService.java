package com.example.travelagent.application;

import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.WeatherInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {

    public WeatherInfo analyze(PlanTripRequest request) {
        String destination = request.destination();
        String riskLevel = resolveRiskLevel(destination);
        String summary = buildSummary(destination, riskLevel);
        String suggestion = buildSuggestion(destination, riskLevel, request.avoid());
        List<String> dailyTips = buildDailyTips(request.days(), riskLevel);

        return new WeatherInfo(destination, summary, riskLevel, suggestion, dailyTips);
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
}
