package com.example.travelagent.knowledge;

import com.example.travelagent.domain.PlanTripRequest;

import java.util.List;

public record KnowledgeDebugResponse(
        PlanTripRequest request,
        String destinationKey,
        boolean dedicatedKnowledgeBase,
        boolean vectorStoreEnabled,
        String retrievalMode,
        String query,
        List<KnowledgeSearchResult> results
) {
}
