package com.example.travelagent.application;

import com.example.travelagent.domain.ItineraryDay;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.TripPlanResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TripPlanResponseValidator {

    public void validate(TripPlanResponse response, PlanTripRequest request) {
        if (response == null) {
            throw new AiResponseParseException("AI 返回的旅行计划为空。");
        }
        if (isBlank(response.destination())) {
            throw new AiResponseParseException("AI 返回的目的地为空。");
        }
        if (!response.destination().trim().equals(request.destination().trim())) {
            throw new AiResponseParseException("AI 返回的目的地与用户请求不一致。");
        }
        if (response.totalDays() != request.days()) {
            throw new AiResponseParseException("AI 返回的旅行天数与用户请求不一致。");
        }
        if (isBlank(response.summary())) {
            throw new AiResponseParseException("AI 返回的行程摘要为空。");
        }

        List<ItineraryDay> days = response.days();
        if (days == null || days.size() != request.days()) {
            throw new AiResponseParseException("AI 返回的每日行程数量与用户请求不一致。");
        }

        for (int index = 0; index < days.size(); index++) {
            validateDay(days.get(index), index + 1);
        }
    }

    private void validateDay(ItineraryDay day, int expectedDayNumber) {
        if (day == null) {
            throw new AiResponseParseException("AI 返回的每日行程存在空项。");
        }
        if (day.day() != expectedDayNumber) {
            throw new AiResponseParseException("AI 返回的每日行程序号不连续。");
        }
        if (
                isBlank(day.theme())
                        || isBlank(day.morning())
                        || isBlank(day.afternoon())
                        || isBlank(day.evening())
                        || isBlank(day.transportTip())
        ) {
            throw new AiResponseParseException("AI 返回的每日行程存在必填字段为空。");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
