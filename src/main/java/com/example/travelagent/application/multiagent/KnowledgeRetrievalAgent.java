package com.example.travelagent.application.multiagent;

import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.knowledge.KnowledgeRetrievalService;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库检索 Agent。
 *
 * <p>职责：根据用户目的地、偏好和避坑项检索 RAG 知识库，
 * 为后面的行程生成 Agent 提供景点、美食、交通、季节等参考资料。</p>
 */
@Component
public class KnowledgeRetrievalAgent implements TravelPlanningSubAgent {

    private final KnowledgeRetrievalService knowledgeRetrievalService;

    public KnowledgeRetrievalAgent(KnowledgeRetrievalService knowledgeRetrievalService) {
        this.knowledgeRetrievalService = knowledgeRetrievalService;
    }

    @Override
    public String name() {
        return "knowledge-retrieval-agent";
    }

    @Override
    public void execute(MultiAgentPlanningContext context) {
        List<KnowledgeSearchResult> references = knowledgeRetrievalService.retrieve(context.request());
        int bestScore = references.stream()
                .mapToInt(KnowledgeSearchResult::score)
                .max()
                .orElse(0);

        context.references(references);
        context.addToolCall(new ToolCallRecord(
                name(),
                "知识库检索 Agent",
                "已完成",
                "命中 %d 条知识库资料，最高匹配分数 %d。".formatted(references.size(), bestScore)
        ));
    }
}
