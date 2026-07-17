package com.example.travelagent.knowledge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "rag.vector-store.enabled", havingValue = "true")
public class DashScopeEmbeddingClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final int dimensions;

    public DashScopeEmbeddingClient(
            @Value("${spring.ai.dashscope.api-key:}") String apiKey,
            @Value("${dashscope.embedding.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
            @Value("${dashscope.embedding.model:text-embedding-v4}") String model,
            @Value("${dashscope.embedding.dimensions:1024}") int dimensions
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
        this.dimensions = dimensions;
    }

    public List<Double> embedDocument(String text) {
        return embed(text);
    }

    public List<Double> embedQuery(String text) {
        return embed(text);
    }

    private List<Double> embed(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 DashScope API Key，无法生成 Embedding。");
        }

        EmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(new EmbeddingRequest(model, text, dimensions, "float"))
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("DashScope Embedding 返回为空。");
        }

        List<Double> embedding = response.data().get(0).embedding();
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalStateException("DashScope Embedding 向量为空。");
        }
        return embedding;
    }

    private record EmbeddingRequest(
            String model,
            String input,
            int dimensions,
            String encoding_format
    ) {
    }

    private record EmbeddingResponse(
            List<EmbeddingData> data
    ) {
    }

    private record EmbeddingData(
            List<Double> embedding
    ) {
    }
}
