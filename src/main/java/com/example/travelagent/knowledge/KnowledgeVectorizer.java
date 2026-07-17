package com.example.travelagent.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class KnowledgeVectorizer {

    private KnowledgeVectorizer() {
    }

    public static Map<String, Double> vectorize(String text) {
        List<String> tokens = tokenize(text);
        Map<String, Double> vector = new HashMap<>();

        for (String token : tokens) {
            vector.merge(token, 1.0, Double::sum);
        }

        double length = Math.sqrt(vector.values().stream()
                .mapToDouble(value -> value * value)
                .sum());

        if (length == 0) {
            return vector;
        }

        vector.replaceAll((token, value) -> value / length);
        return vector;
    }

    public static double cosineSimilarity(Map<String, Double> left, Map<String, Double> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }

        Map<String, Double> smaller = left.size() <= right.size() ? left : right;
        Map<String, Double> larger = left.size() <= right.size() ? right : left;

        double dotProduct = 0;
        for (Map.Entry<String, Double> entry : smaller.entrySet()) {
            dotProduct += entry.getValue() * larger.getOrDefault(entry.getKey(), 0.0);
        }

        return dotProduct;
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> tokens = new ArrayList<>();
        String normalized = text.toLowerCase(Locale.ROOT);
        StringBuilder chineseBuffer = new StringBuilder();
        StringBuilder latinBuffer = new StringBuilder();

        for (int index = 0; index < normalized.length(); index++) {
            char current = normalized.charAt(index);
            if (isChinese(current)) {
                flushLatinToken(tokens, latinBuffer);
                chineseBuffer.append(current);
            } else if (Character.isLetterOrDigit(current)) {
                flushChineseTokens(tokens, chineseBuffer);
                latinBuffer.append(current);
            } else {
                flushChineseTokens(tokens, chineseBuffer);
                flushLatinToken(tokens, latinBuffer);
            }
        }

        flushChineseTokens(tokens, chineseBuffer);
        flushLatinToken(tokens, latinBuffer);
        return tokens;
    }

    private static void flushChineseTokens(List<String> tokens, StringBuilder buffer) {
        if (buffer.isEmpty()) {
            return;
        }

        String value = buffer.toString();
        for (int index = 0; index < value.length(); index++) {
            tokens.add(String.valueOf(value.charAt(index)));
        }
        for (int index = 0; index < value.length() - 1; index++) {
            tokens.add(value.substring(index, index + 2));
        }
        if (value.length() >= 3) {
            for (int index = 0; index < value.length() - 2; index++) {
                tokens.add(value.substring(index, index + 3));
            }
        }
        buffer.setLength(0);
    }

    private static void flushLatinToken(List<String> tokens, StringBuilder buffer) {
        if (!buffer.isEmpty()) {
            tokens.add(buffer.toString());
            buffer.setLength(0);
        }
    }

    private static boolean isChinese(char value) {
        Character.UnicodeScript script = Character.UnicodeScript.of(value);
        return script == Character.UnicodeScript.HAN;
    }
}
