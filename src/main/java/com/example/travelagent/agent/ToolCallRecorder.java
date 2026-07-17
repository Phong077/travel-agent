package com.example.travelagent.agent;

import com.example.travelagent.domain.ToolCallRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToolCallRecorder {

    private final ThreadLocal<List<ToolCallRecord>> records = ThreadLocal.withInitial(ArrayList::new);

    public void record(String name, String displayName, String status, String detail) {
        records.get().add(new ToolCallRecord(name, displayName, status, detail));
    }

    public List<ToolCallRecord> snapshot() {
        return List.copyOf(records.get());
    }

    public void clear() {
        records.remove();
    }
}
