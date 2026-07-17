package com.example.travelagent.knowledge;

import com.example.travelagent.domain.PlanTripRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "rag.vector-store.enabled", havingValue = "true")
public class PgVectorKnowledgeStore {

    private static final Logger log = LoggerFactory.getLogger(PgVectorKnowledgeStore.class);
    private static final String COMMON_KEY = "common";

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int dimensions;
    private final KnowledgeBaseLoader knowledgeBaseLoader;
    private final DashScopeEmbeddingClient embeddingClient;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public PgVectorKnowledgeStore(
            @Value("${rag.vector-store.jdbc-url:jdbc:postgresql://localhost:5432/travel_agent}") String jdbcUrl,
            @Value("${rag.vector-store.username:travel_agent}") String username,
            @Value("${rag.vector-store.password:travel_agent}") String password,
            @Value("${dashscope.embedding.dimensions:1024}") int dimensions,
            KnowledgeBaseLoader knowledgeBaseLoader,
            DashScopeEmbeddingClient embeddingClient
    ) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.dimensions = dimensions;
        this.knowledgeBaseLoader = knowledgeBaseLoader;
        this.embeddingClient = embeddingClient;
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
            return searchByEmbedding(destinationKey, queryEmbedding, topK);
        } catch (RuntimeException | SQLException exception) {
            log.warn(
                    "pgvector retrieval failed, fallback to local retrieval. destination={}, reason={}",
                    request.destination(),
                    exception.getMessage()
            );
            return List.of();
        }
    }

    private void initializeIfNecessary() throws SQLException {
        if (initialized.get()) {
            return;
        }

        synchronized (initialized) {
            if (initialized.get()) {
                return;
            }

            try (Connection connection = openConnection()) {
                createSchema(connection);
                importKnowledgeDocuments(connection, knowledgeBaseLoader.loadDocuments());
                initialized.set(true);
            }
        }
    }

    private void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS vector");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS knowledge_chunks (
                        id BIGSERIAL PRIMARY KEY,
                        destination_key VARCHAR(64) NOT NULL,
                        title TEXT NOT NULL,
                        source TEXT NOT NULL,
                        content TEXT NOT NULL,
                        content_hash VARCHAR(64) NOT NULL UNIQUE,
                        embedding vector(%d) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
                    )
                    """.formatted(dimensions));
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_destination
                    ON knowledge_chunks(destination_key)
                    """);
        }
    }

    private void importKnowledgeDocuments(Connection connection, List<KnowledgeDocument> documents) throws SQLException {
        int imported = 0;
        for (KnowledgeDocument document : documents) {
            String hash = contentHash(document);
            if (existsByHash(connection, hash)) {
                continue;
            }

            List<Double> embedding = embeddingClient.embedDocument(document.title() + "\n" + document.content());
            insertDocument(connection, document, hash, embedding);
            imported++;
        }

        log.info("pgvector knowledge import completed, newChunks={}", imported);
    }

    private boolean existsByHash(Connection connection, String hash) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM knowledge_chunks WHERE content_hash = ? LIMIT 1"
        )) {
            statement.setString(1, hash);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insertDocument(
            Connection connection,
            KnowledgeDocument document,
            String hash,
            List<Double> embedding
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO knowledge_chunks(destination_key, title, source, content, content_hash, embedding)
                VALUES (?, ?, ?, ?, ?, ?::vector)
                ON CONFLICT (content_hash) DO UPDATE SET
                    destination_key = EXCLUDED.destination_key,
                    title = EXCLUDED.title,
                    source = EXCLUDED.source,
                    content = EXCLUDED.content,
                    embedding = EXCLUDED.embedding,
                    updated_at = now()
                """)) {
            statement.setString(1, document.destinationKey());
            statement.setString(2, document.title());
            statement.setString(3, document.source());
            statement.setString(4, document.content());
            statement.setString(5, hash);
            statement.setString(6, toPgVector(embedding));
            statement.executeUpdate();
        }
    }

    private List<KnowledgeSearchResult> searchByEmbedding(
            String destinationKey,
            List<Double> queryEmbedding,
            int topK
    ) throws SQLException {
        String vector = toPgVector(queryEmbedding);
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT title, source, content,
                            GREATEST(1, ROUND((1 - (embedding <=> ?::vector)) * 100))::int AS score
                     FROM knowledge_chunks
                     WHERE destination_key = ? OR destination_key = ?
                     ORDER BY embedding <=> ?::vector
                     LIMIT ?
                     """)) {
            statement.setString(1, vector);
            statement.setString(2, COMMON_KEY);
            statement.setString(3, destinationKey);
            statement.setString(4, vector);
            statement.setInt(5, topK);

            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<KnowledgeSearchResult> results = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    results.add(new KnowledgeSearchResult(
                            resultSet.getString("title"),
                            resultSet.getString("source"),
                            resultSet.getString("content"),
                            resultSet.getInt("score")
                    ));
                }
                return results;
            }
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
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
