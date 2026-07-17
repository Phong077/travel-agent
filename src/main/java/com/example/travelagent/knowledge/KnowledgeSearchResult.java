package com.example.travelagent.knowledge;

public record KnowledgeSearchResult(
        //命中资料的标题，比如“都江堰与青城山”
        String title,
        //来源文件，比如“sichuan-attractions.md”
        String source,
        //命中的具体内容片段
        String content,
        //匹配分数。当前由关键词权重分和本地向量相似度分共同组成。
        int score
) {
}
