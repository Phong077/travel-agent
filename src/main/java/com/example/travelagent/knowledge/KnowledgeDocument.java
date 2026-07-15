package com.example.travelagent.knowledge;

public record KnowledgeDocument(
        //这一段知识的标题，比如“都江堰与青城山”
        String title,
        //来源文件，比如“sichuan-attractions.md”
        String source,
        //这一段知识的正文内容
        String content
) {
}