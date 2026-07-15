package com.example.travelagent.knowledge;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeBaseLoader {

    private static final String KNOWLEDGE_PATH = "classpath:knowledge/*.md";

    public List<KnowledgeDocument> loadDocuments() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(KNOWLEDGE_PATH);

            List<KnowledgeDocument> documents = new ArrayList<>();
            for (Resource resource : resources) {
                String source = resource.getFilename();
                String content = resource.getContentAsString(StandardCharsets.UTF_8);
                documents.addAll(splitMarkdown(source, content));
            }
            return documents;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load knowledge base files.", e);
        }
    }

    private List<KnowledgeDocument> splitMarkdown(String source, String content) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        String[] sections = content.split("(?m)^## ");

        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("# ")) {
                continue;
            }

            int lineBreakIndex = trimmed.indexOf('\n');
            if (lineBreakIndex < 0) {
                continue;
            }

            String title = trimmed.substring(0, lineBreakIndex).trim();
            String body = trimmed.substring(lineBreakIndex + 1).trim();
            if (!title.isEmpty() && !body.isEmpty()) {
                documents.add(new KnowledgeDocument(title, source, body));
            }
        }

        return documents;
    }
}
