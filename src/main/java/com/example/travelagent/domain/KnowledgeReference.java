package com.example.travelagent.domain;

/**
 * 返回给前端的知识库引用信息。
 *
 * 它用于说明本次旅行计划参考了哪些知识库资料。
 * 和 KnowledgeSearchResult 不同，这里只返回 snippet，不直接暴露完整 content。
 */
public record KnowledgeReference(
         //* 知识片段标题，例如：成都、成都美食、都江堰与青城山。
        String title,
       //来源文件，例如：sichuan-attractions.md。
        String source,
         // 引用摘要。
         //* 这是从知识库正文中截取的一小段内容，用于前端展示。
        String snippet,
        //匹配分数。
       //第一版中，score 表示命中的关键词数量。
        int score
) {
}