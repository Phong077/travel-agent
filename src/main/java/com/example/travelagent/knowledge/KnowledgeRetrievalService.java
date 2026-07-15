package com.example.travelagent.knowledge;

import com.example.travelagent.domain.PlanTripRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class KnowledgeRetrievalService {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetrievalService.class);

    private static final int TOP_K = 5;

    private record WeightedKeyword(String value, int weight) {
    }
    private final List<KnowledgeDocument> documents;

    public KnowledgeRetrievalService(KnowledgeBaseLoader knowledgeBaseLoader) {
        this.documents = knowledgeBaseLoader.loadDocuments();
    }

    public List<KnowledgeSearchResult> retrieve(PlanTripRequest request) {
        List<WeightedKeyword> keywords = buildKeywords(request);

        log.info("Retrieval keywords: {}", formatKeywords(keywords));

        List<KnowledgeSearchResult> results = documents.stream()
                .map(document -> toSearchResult(document, keywords))
                .filter(result -> result.score() > 0)
                .sorted(Comparator.comparingInt(KnowledgeSearchResult::score).reversed())
                .limit(TOP_K)
                .toList();

        results.forEach(result -> log.info(
                "Knowledge matched: title={}, source={}, score={}",
                result.title(),
                result.source(),
                result.score()
        ));

        return results;
    }
    private String formatKeywords(List<WeightedKeyword> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.value() + "(" + keyword.weight() + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
    }
    private KnowledgeSearchResult toSearchResult(KnowledgeDocument document, List<WeightedKeyword> keywords) {
        int score = 0;
        String searchableText = document.title() + "\n" + document.content();

        for (WeightedKeyword keyword : keywords) {
            if (searchableText.contains(keyword.value())) {
                score += keyword.weight();
            }
        }

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

        keywords.add(new WeightedKeyword("四川", 1));
        keywords.add(new WeightedKeyword("成都", 1));
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
