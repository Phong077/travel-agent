package com.example.travelagent.api;

import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.knowledge.KnowledgeDebugResponse;
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
 * 这些接口用于验证 RAG 的“检索阶段”是否命中了正确资料。
 * search 只返回命中的片段，debug 会额外返回目的地解析、检索模式和 query。
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeRetrievalService knowledgeRetrievalService;

    public KnowledgeController(KnowledgeRetrievalService knowledgeRetrievalService) {
        this.knowledgeRetrievalService = knowledgeRetrievalService;
    }

    /**
     * 轻量检索接口：只返回命中的知识片段。
     */
    @PostMapping("/search")
    public ApiResponse<List<KnowledgeSearchResult>> search(@Valid @RequestBody PlanTripRequest request) {
        return ApiResponse.success(knowledgeRetrievalService.retrieve(request));
    }

    /**
     * 调试接口：返回完整 RAG 检索链路，方便前端调试页和面试演示。
     */
    @PostMapping("/debug")
    public ApiResponse<KnowledgeDebugResponse> debug(@Valid @RequestBody PlanTripRequest request) {
        return ApiResponse.success(knowledgeRetrievalService.debug(request));
    }
}
