package com.example.travelagent.application;

import com.example.travelagent.domain.ToolCallRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AgentToolCallValidator {

    private static final Set<String> REQUIRED_TOOL_NAMES = Set.of(
            "analyzeTravelBudget",
            "analyzeTravelWeather",
            "searchTravelKnowledge"
    );

    public void validate(List<ToolCallRecord> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            throw new AiResponseParseException("AI Agent 未调用任何工具。");
        }

        Set<String> calledToolNames = toolCalls.stream()
                .map(ToolCallRecord::name)
                .collect(Collectors.toSet());

        List<String> missingToolNames = REQUIRED_TOOL_NAMES.stream()
                .filter(toolName -> !calledToolNames.contains(toolName))
                .sorted()
                .toList();

        if (!missingToolNames.isEmpty()) {
            throw new AiResponseParseException("AI Agent 缺少必要工具调用：" + String.join("、", missingToolNames));
        }
    }
}
