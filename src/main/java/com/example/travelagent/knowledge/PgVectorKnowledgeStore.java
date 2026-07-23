package com.example.travelagent.knowledge;

import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.knowledge.persistence.KnowledgeChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "rag.vector-store.enabled", havingValue = "true")
public class PgVectorKnowledgeStore {

    private static final Logger log = LoggerFactory.getLogger(PgVectorKnowledgeStore.class);

    private final int dimensions;
    private final KnowledgeBaseLoader knowledgeBaseLoader;
    private final DashScopeEmbeddingClient embeddingClient;
    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public PgVectorKnowledgeStore(
            @Value("${dashscope.embedding.dimensions:1024}") int dimensions,
            KnowledgeBaseLoader knowledgeBaseLoader,
            DashScopeEmbeddingClient embeddingClient,
            KnowledgeChunkRepository knowledgeChunkRepository
    ) {
        this.dimensions = dimensions;
        this.knowledgeBaseLoader = knowledgeBaseLoader;
        this.embeddingClient = embeddingClient;
        this.knowledgeChunkRepository = knowledgeChunkRepository;
    }

    public List<KnowledgeSearchResult> search(
            PlanTripRequest request,
            String destinationKey,
            String query,
            int topK
    ) {
        try {
            initializeIfNecessary();
            List<Double> queryEmbedding = embeddingClient.embedQuery(query);
            return knowledgeChunkRepository.searchByEmbedding(destinationKey, toPgVector(queryEmbedding), topK);
        } catch (RuntimeException exception) {
            log.warn(
                    "pgvector retrieval failed, fallback to local retrieval. destination={}, reason={}",
                    request.destination(),
                    exception.getMessage()
            );
            return List.of();
        }
    }

    private void initializeIfNecessary() {
        if (initialized.get()) {
            return;
        }

        synchronized (initialized) {
            if (initialized.get()) {
                return;
            }

            knowledgeChunkRepository.createSchema(dimensions);
            importKnowledgeDocuments(knowledgeBaseLoader.loadDocuments());
            initialized.set(true);
        }
    }

    private void importKnowledgeDocuments(List<KnowledgeDocument> documents) {
        int imported = 0;
        for (KnowledgeDocument document : documents) {
            String hash = contentHash(document);
            if (knowledgeChunkRepository.existsByHash(hash)) {
                continue;
            }

            List<Double> embedding = embeddingClient.embedDocument(document.title() + "\n" + document.content());
            knowledgeChunkRepository.upsert(document, hash, toPgVector(embedding));
            imported++;
        }

        log.info("pgvector knowledge import completed, newChunks={}", imported);
    }

    private String contentHash(KnowledgeDocument document) {
        String raw = document.destinationKey() + "\n" + document.title() + "\n" + document.source() + "\n" + document.content();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }

    private String toPgVector(List<Double> embedding) {
        return embedding.stream()
                .map(value -> Double.toString(value == null ? 0.0 : value))
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }
}
