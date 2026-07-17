package com.example.travelagent.agent;

import com.example.travelagent.domain.ToolCallRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToolCallRecorderTests {

    @Test
    void shouldRecordAndClearToolCalls() {
        ToolCallRecorder recorder = new ToolCallRecorder();

        recorder.record("analyzeTravelBudget", "预算分析工具", "已调用", "人均预算 3000 元");
        recorder.record("searchTravelKnowledge", "知识库检索工具", "已调用", "命中 5 条引用");

        List<ToolCallRecord> snapshot = recorder.snapshot();

        assertThat(snapshot).hasSize(2);
        assertThat(snapshot)
                .extracting(ToolCallRecord::name)
                .containsExactly("analyzeTravelBudget", "searchTravelKnowledge");

        recorder.clear();

        assertThat(recorder.snapshot()).isEmpty();
    }
}
