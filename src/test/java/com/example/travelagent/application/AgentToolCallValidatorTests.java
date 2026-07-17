package com.example.travelagent.application;

import com.example.travelagent.domain.ToolCallRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentToolCallValidatorTests {

    private final AgentToolCallValidator validator = new AgentToolCallValidator();

    @Test
    void shouldAcceptAllRequiredToolCalls() {
        validator.validate(List.of(
                toolCall("analyzeTravelBudget"),
                toolCall("analyzeTravelWeather"),
                toolCall("searchTravelKnowledge")
        ));
    }

    @Test
    void shouldRejectEmptyToolCalls() {
        assertThatThrownBy(() -> validator.validate(List.of()))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("未调用任何工具");
    }

    @Test
    void shouldRejectMissingRequiredToolCall() {
        assertThatThrownBy(() -> validator.validate(List.of(
                toolCall("analyzeTravelBudget"),
                toolCall("searchTravelKnowledge")
        )))
                .isInstanceOf(AiResponseParseException.class)
                .hasMessageContaining("analyzeTravelWeather");
    }

    private ToolCallRecord toolCall(String name) {
        return new ToolCallRecord(name, name, "已调用", "测试工具调用");
    }
}
