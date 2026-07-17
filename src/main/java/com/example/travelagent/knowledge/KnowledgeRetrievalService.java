package com.example.travelagent.knowledge;

import com.example.travelagent.domain.PlanTripRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KnowledgeRetrievalService {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetrievalService.class);

    private static final int TOP_K = 5;
    private static final String COMMON_KEY = "common";

    private record WeightedKeyword(String value, int weight) {
    }

    private final List<KnowledgeDocument> documents;
    private final KnowledgeVectorIndex vectorIndex;
    private final DestinationResolver destinationResolver;
    private final Optional<PgVectorKnowledgeStore> pgVectorKnowledgeStore;

    @Autowired
    public KnowledgeRetrievalService(
            KnowledgeBaseLoader knowledgeBaseLoader,
            DestinationResolver destinationResolver,
            Optional<PgVectorKnowledgeStore> pgVectorKnowledgeStore
    ) {
        this.documents = knowledgeBaseLoader.loadDocuments();
        this.vectorIndex = new KnowledgeVectorIndex(documents);
        this.destinationResolver = destinationResolver;
        this.pgVectorKnowledgeStore = pgVectorKnowledgeStore;
    }

    public KnowledgeRetrievalService(
            KnowledgeBaseLoader knowledgeBaseLoader,
            DestinationResolver destinationResolver
    ) {
        this(knowledgeBaseLoader, destinationResolver, Optional.empty());
    }

    public List<KnowledgeSearchResult> retrieve(PlanTripRequest request) {
        String destinationKey = destinationResolver.resolve(request.destination());
        List<WeightedKeyword> keywords = buildKeywords(request);
        String query = buildVectorQuery(request, keywords);
        List<KnowledgeSearchResult> pgVectorResults = retrieveFromPgVector(request, destinationKey, query);
        if (!pgVectorResults.isEmpty()) {
            logMatchedResults("pgvector", pgVectorResults);
            return pgVectorResults;
        }

        Map<KnowledgeDocument, Double> vectorScores = vectorIndex.search(query);

        log.info("Retrieval destinationKey={}, keywords={}, vectorQuery={}", destinationKey, formatKeywords(keywords), query);

        List<KnowledgeSearchResult> results = documents.stream()
                .filter(document -> isSearchableForDestination(document, destinationKey))
                .map(document -> toSearchResult(document, keywords, vectorScores.getOrDefault(document, 0.0)))
                .filter(result -> result.score() > 0)
                .sorted(Comparator.comparingInt(KnowledgeSearchResult::score).reversed())
                .limit(TOP_K)
                .toList();

        logMatchedResults("local-hybrid", results);

        return results;
    }

    private List<KnowledgeSearchResult> retrieveFromPgVector(
            PlanTripRequest request,
            String destinationKey,
            String query
    ) {
        if (pgVectorKnowledgeStore.isEmpty()) {
            return List.of();
        }

        List<KnowledgeSearchResult> results = pgVectorKnowledgeStore.get().search(request, destinationKey, query, TOP_K);
        if (!results.isEmpty()) {
            log.info("Retrieval destinationKey={}, vectorStore=pgvector, vectorQuery={}", destinationKey, query);
        }
        return results;
    }

    private void logMatchedResults(String retrievalMode, List<KnowledgeSearchResult> results) {
        results.forEach(result -> log.info(
                "Knowledge matched: mode={}, title={}, source={}, score={}",
                retrievalMode,
                result.title(),
                result.source(),
                result.score()
        ));
    }

    private boolean isSearchableForDestination(KnowledgeDocument document, String destinationKey) {
        return COMMON_KEY.equals(document.destinationKey()) || document.destinationKey().equals(destinationKey);
    }

    private String formatKeywords(List<WeightedKeyword> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.value() + "(" + keyword.weight() + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
    }

    private KnowledgeSearchResult toSearchResult(
            KnowledgeDocument document,
            List<WeightedKeyword> keywords,
            double vectorScore
    ) {
        int score = 0;
        String searchableText = document.title() + "\n" + document.content();

        for (WeightedKeyword keyword : keywords) {
            if (searchableText.contains(keyword.value())) {
                score += keyword.weight();
            }
        }

        int vectorPoints = (int) Math.round(vectorScore * 10);
        score += vectorPoints;

        return new KnowledgeSearchResult(
                document.title(),
                document.source(),
                document.content(),
                score
        );
    }

    private List<WeightedKeyword> buildKeywords(PlanTripRequest request) {
        List<WeightedKeyword> keywords = new ArrayList<>();

        addIfPresent(keywords, request.destination(), 3);
        addAllIfPresent(keywords, request.preferences(), 3);

        // 默认补充通用旅行关键词，避免用户偏好过少时检索条件太窄。
        keywords.add(new WeightedKeyword("交通", 1));
        keywords.add(new WeightedKeyword("美食", 1));

        return keywords.stream()
                .filter(keyword -> keyword.value() != null && !keyword.value().isBlank())
                .collect(java.util.stream.Collectors.toMap(
                        WeightedKeyword::value,
                        keyword -> keyword,
                        (left, right) -> left.weight() >= right.weight() ? left : right
                ))
                .values()
                .stream()
                .toList();
    }

    private String buildVectorQuery(PlanTripRequest request, List<WeightedKeyword> keywords) {
        List<String> parts = new ArrayList<>();
        parts.add(request.destination());
        parts.add(request.departureCity());
        parts.add("旅行 行程 景点 美食 交通 天气 住宿 节奏");
        keywords.forEach(keyword -> parts.add(keyword.value()));
        if (request.avoid() != null) {
            parts.addAll(request.avoid());
        }

        return parts.stream()
                .filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    private void addIfPresent(List<WeightedKeyword> keywords, String value, int weight) {
        if (value != null && !value.isBlank()) {
            keywords.add(new WeightedKeyword(value, weight));
        }
    }

    private void addAllIfPresent(List<WeightedKeyword> keywords, List<String> values, int weight) {
        if (values != null) {
            values.forEach(value -> addIfPresent(keywords, value, weight));
        }
    }
}
