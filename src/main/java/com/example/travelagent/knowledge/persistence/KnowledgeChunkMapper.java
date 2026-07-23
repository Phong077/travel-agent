package com.example.travelagent.knowledge.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunkEntity> {

    @Update("CREATE EXTENSION IF NOT EXISTS vector")
    void createVectorExtension();

    @Update("""
            CREATE TABLE IF NOT EXISTS knowledge_chunks (
                id BIGSERIAL PRIMARY KEY,
                destination_key VARCHAR(64) NOT NULL,
                title TEXT NOT NULL,
                source TEXT NOT NULL,
                content TEXT NOT NULL,
                content_hash VARCHAR(64) NOT NULL UNIQUE,
                embedding vector(${dimensions}) NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
            )
            """)
    void createKnowledgeChunksTable(@Param("dimensions") int dimensions);

    @Update("""
            CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_destination
            ON knowledge_chunks(destination_key)
            """)
    void createDestinationIndex();

    @Select("SELECT COUNT(1) FROM knowledge_chunks WHERE content_hash = #{hash}")
    long countByContentHash(@Param("hash") String hash);

    @Insert("""
            INSERT INTO knowledge_chunks(destination_key, title, source, content, content_hash, embedding)
            VALUES (#{destinationKey}, #{title}, #{source}, #{content}, #{contentHash}, CAST(#{embedding} AS vector))
            ON CONFLICT (content_hash) DO UPDATE SET
                destination_key = EXCLUDED.destination_key,
                title = EXCLUDED.title,
                source = EXCLUDED.source,
                content = EXCLUDED.content,
                embedding = EXCLUDED.embedding,
                updated_at = now()
            """)
    int upsertKnowledgeChunk(
            @Param("destinationKey") String destinationKey,
            @Param("title") String title,
            @Param("source") String source,
            @Param("content") String content,
            @Param("contentHash") String contentHash,
            @Param("embedding") String embedding
    );

    @Select("""
            SELECT title, source, content,
                   GREATEST(
                       1,
                       CAST(ROUND((1 - (embedding <=> CAST(#{vector} AS vector))) * 100) AS INTEGER)
                   ) AS score
            FROM knowledge_chunks
            WHERE destination_key = #{commonKey} OR destination_key = #{destinationKey}
            ORDER BY embedding <=> CAST(#{vector} AS vector)
            LIMIT #{topK}
            """)
    List<KnowledgeChunkSearchRow> searchByEmbedding(
            @Param("commonKey") String commonKey,
            @Param("destinationKey") String destinationKey,
            @Param("vector") String vector,
            @Param("topK") int topK
    );
}
