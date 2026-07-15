package com.example.travelagent.knowledge;

public record KnowledgeSearchResult(
        //命中资料的标题，比如“都江堰与青城山”
        String title,
        //来源文件，比如“sichuan-attractions.md”
        String source,
        //命中的具体内容片段
        String content,
        //匹配分数。第一版我们用关键词命中次数来表示相关度。
        int score
) {
}