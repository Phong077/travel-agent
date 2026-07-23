package com.example.travelagent.knowledge.persistence;

import com.example.travelagent.knowledge.KnowledgeDocument;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "rag.vector-store.enabled", havingValue = "true")
public class KnowledgeChunkRepository {

    private static final String COMMON_KEY = "common";

    private final KnowledgeChunkMapper knowledgeChunkMapper;

    public KnowledgeChunkRepository(KnowledgeChunkMapper knowledgeChunkMapper) {
        this.knowledgeChunkMapper = knowledgeChunkMapper;
    }

    public void createSchema(int dimensions) {
        knowledgeChunkMapper.createVectorExtension();
        knowledgeChunkMapper.createKnowledgeChunksTable(dimensions);
        knowledgeChunkMapper.createDestinationIndex();
    }

    public boolean existsByHash(String hash) {
        return knowledgeChunkMapper.countByContentHash(hash) > 0;
    }

    public void upsert(KnowledgeDocument document, String hash, String embedding) {
        knowledgeChunkMapper.upsertKnowledgeChunk(
                document.destinationKey(),
                document.title(),
                document.source(),
                document.content(),
                hash,
                embedding
        );
    }

    public List<KnowledgeSearchResult> searchByEmbedding(String destinationKey, String vector, int topK) {
        return knowledgeChunkMapper.searchByEmbedding(COMMON_KEY, destinationKey, vector, topK)
                .stream()
                .map(row -> new KnowledgeSearchResult(
                        row.getTitle(),
                        row.getSource(),
                        row.getContent(),
                        row.getScore() == null ? 0 : row.getScore()
                ))
                .toList();
    }
}
