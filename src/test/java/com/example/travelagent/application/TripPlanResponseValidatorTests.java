package com.example.travelagent.application;

import com.example.travelagent.domain.ItineraryDay;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.TripPlanResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripPlanResponseValidatorTests {

    private final TripPlanResponseValidator validator = new TripPlanResponseValidator();

    @Test
    void shouldAcceptValidTripPlanResponse() {
        validator.validate(
                createResponse(2, List.of(createDay(1), createDay(2))),
                createRequest(2)
        );
    }

    @Test
    void shouldRejectMismatchedDayCount() {
        assertThatThrownBy(() -> validator.validate(
                createResponse(2, List.of(createDay(1))),
                createRequest(2)
        ))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("每日行程数量");
    }

    @Test
    void shouldRejectMismatchedDestination() {
        assertThatThrownBy(() -> validator.validate(
                new TripPlanResponse(
                        "四川",
                        2,
                        "四川旅行计划。",
                        List.of(createDay(1), createDay(2)),
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                createRequest(2)
        ))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("目的地");
    }

    @Test
    void shouldRejectBlankDayField() {
        ItineraryDay invalidDay = new ItineraryDay(
                1,
                " ",
                "上午安排",
                "下午安排",
                "晚上安排",
                "交通建议"
        );

        assertThatThrownBy(() -> validator.validate(
                createResponse(1, List.of(invalidDay)),
                createRequest(1)
        ))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("必填字段");
    }

    private PlanTripRequest createRequest(int days) {
        return new PlanTripRequest(
                "重庆",
                "云南",
                days,
                2,
                6000,
                List.of("美食"),
                List.of("太早起床")
        );
    }

    private TripPlanResponse createResponse(int totalDays, List<ItineraryDay> days) {
        return new TripPlanResponse(
                "云南",
                totalDays,
                "云南轻松旅行计划。",
                days,
                null,
                null,
                null,
                null,
                null
        );
    }

    private ItineraryDay createDay(int day) {
        return new ItineraryDay(
                day,
                "第 %d 天主题".formatted(day),
                "上午安排",
                "下午安排",
                "晚上安排",
                "交通建议"
        );
    }
}
