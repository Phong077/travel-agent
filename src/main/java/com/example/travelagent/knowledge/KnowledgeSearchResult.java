package com.example.travelagent.knowledge;

public record KnowledgeSearchResult(
        // 命中资料的标题，比如“深圳滨海与城市公园”。
        String title,
        // 来源文件，比如“guangdong/attractions.md”。
        String source,
        // 命中的具体内容片段。
        String content,
        // 匹配分数。pgvector 模式来自向量相似度，本地模式来自关键词和本地向量分。
        int score
) {
}
