package com.example.travelagent.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeVectorIndex {

    private final List<IndexedKnowledgeDocument> indexedDocuments;

    public KnowledgeVectorIndex(List<KnowledgeDocument> documents) {
        this.indexedDocuments = documents.stream()
                .map(document -> new IndexedKnowledgeDocument(
                        document,
                        KnowledgeVectorizer.vectorize(document.title() + "\n" + document.content())
                ))
                .toList();
    }

    public Map<KnowledgeDocument, Double> search(String query) {
        Map<String, Double> queryVector = KnowledgeVectorizer.vectorize(query);
        Map<KnowledgeDocument, Double> scores = new HashMap<>();

        for (IndexedKnowledgeDocument indexedDocument : indexedDocuments) {
            double similarity = KnowledgeVectorizer.cosineSimilarity(queryVector, indexedDocument.vector());
            scores.put(indexedDocument.document(), similarity);
        }

        return scores;
    }

    private record IndexedKnowledgeDocument(
            KnowledgeDocument document,
            Map<String, Double> vector
    ) {
    }
}
