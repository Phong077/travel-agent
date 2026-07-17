package com.example.travelagent.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TripPlanResponseSerializationTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeToolCallRecords() throws Exception {
        TripPlanResponse response = new TripPlanResponse(
                "云南",
                3,
                "昆明、大理、丽江三日轻松游。",
                List.of(new ItineraryDay(
                        1,
                        "昆明初印象",
                        "抵达昆明并入住酒店。",
                        "游览翠湖和周边街区。",
                        "品尝云南米线。",
                        "市内优先使用地铁和步行。"
                )),
                List.of(new KnowledgeReference(
                        "昆明",
                        "yunnan/attractions.md",
                        "昆明适合作为云南旅行起点。",
                        7
                )),
                new BudgetAnalysis(3000, 1000, "舒适", "预算较宽裕，可以选择交通便利酒店。"),
                new WeatherInfo(
                        "云南",
                        "昼夜温差较明显。",
                        "昼夜温差",
                        "建议准备外套并关注实时天气。",
                        List.of("第 1 天注意早晚温差。")
                ),
                List.of(new ToolCallRecord(
                        "analyzeTravelBudget",
                        "预算分析工具",
                        "已调用",
                        "人均预算 3000 元，每日人均 1000 元，预算等级：舒适"
                )),
                new GenerationMetadata("agent", 1, true)
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"toolCalls\"");
        assertThat(json).contains("\"displayName\":\"预算分析工具\"");
        assertThat(json).contains("\"status\":\"已调用\"");
        assertThat(json).contains("\"generationMetadata\"");
        assertThat(json).contains("\"mode\":\"agent\"");
    }
}
