package com.example.travelagent.api;

import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.knowledge.KnowledgeRetrievalService;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识库检索调试接口。
 *
 * 这个接口主要用于验证 RAG 的“检索阶段”是否正常工作。
 * 它不会调用大模型，只会返回本地知识库中命中的资料。
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    /**
     * 知识库检索服务。
     *
     * 负责根据用户的旅行需求，从本地 Markdown 知识库中找出相关资料。
     */
    private final KnowledgeRetrievalService knowledgeRetrievalService;

    /**
     * 构造方法注入。
     *
     * Spring 创建 KnowledgeController 时，会自动把 KnowledgeRetrievalService 传进来。
     */
    public KnowledgeController(KnowledgeRetrievalService knowledgeRetrievalService) {
        this.knowledgeRetrievalService = knowledgeRetrievalService;
    }

    /**
     * 检索知识库。
     *
     * 请求路径：
     * POST /api/knowledge/search
     *
     * 请求体使用 PlanTripRequest，和旅行规划接口保持一致。
     * 这样我们可以用同一份用户需求，分别测试：
     * 1. 检索阶段命中了哪些资料
     * 2. 最终旅行规划用了哪些资料
     */
    @PostMapping("/search")
    public ApiResponse<List<KnowledgeSearchResult>> search(@Valid @RequestBody PlanTripRequest request) {
        return ApiResponse.success(knowledgeRetrievalService.retrieve(request));
    }
}