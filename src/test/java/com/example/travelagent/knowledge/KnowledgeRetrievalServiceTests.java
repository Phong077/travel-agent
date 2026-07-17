package com.example.travelagent.knowledge;

import com.example.travelagent.domain.PlanTripRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeRetrievalServiceTests {

    private final KnowledgeRetrievalService retrievalService = new KnowledgeRetrievalService(
            new KnowledgeBaseLoader(),
            new DestinationResolver()
    );

    @Test
    void shouldRetrieveSichuanKnowledgeForSichuanDestination() {
        PlanTripRequest request = new PlanTripRequest(
                "重庆",
                "四川",
                5,
                2,
                6000,
                List.of("美食", "自然风景"),
                List.of("太早起床")
        );

        List<KnowledgeSearchResult> results = retrievalService.retrieve(request);

        assertThat(results).isNotEmpty();
        assertThat(results)
                .extracting(KnowledgeSearchResult::source)
                .anyMatch(source -> source.startsWith("sichuan/"));
    }

    @Test
    void shouldRetrieveYunnanKnowledgeForYunnanDestination() {
        PlanTripRequest request = new PlanTripRequest(
                "重庆",
                "云南",
                6,
                2,
                7000,
                List.of("美食", "自然风景"),
                List.of("频繁换酒店")
        );

        List<KnowledgeSearchResult> results = retrievalService.retrieve(request);

        assertThat(results).isNotEmpty();
        assertThat(results)
                .extracting(KnowledgeSearchResult::source)
                .anyMatch(source -> source.startsWith("yunnan/"));
    }

    @Test
    void shouldFallbackToCommonKnowledgeForUnknownDestination() {
        PlanTripRequest request = new PlanTripRequest(
                "重庆",
                "新疆",
                6,
                2,
                8000,
                List.of("美食", "自然风景"),
                List.of("频繁换酒店")
        );

        List<KnowledgeSearchResult> results = retrievalService.retrieve(request);

        assertThat(results).isNotEmpty();
        assertThat(results)
                .extracting(KnowledgeSearchResult::source)
                .allMatch(source -> source.startsWith("common/"));
    }

    @Test
    void shouldUseVectorSimilarityForRelatedTravelIntent() {
        PlanTripRequest request = new PlanTripRequest(
                "重庆",
                "四川",
                5,
                2,
                6000,
                List.of("山水湖泊", "摄影"),
                List.of("过度奔波")
        );

        List<KnowledgeSearchResult> results = retrievalService.retrieve(request);

        assertThat(results).isNotEmpty();
        assertThat(results)
                .extracting(KnowledgeSearchResult::title)
                .anyMatch(title -> title.contains("九寨沟") || title.contains("川西"));
    }
}
