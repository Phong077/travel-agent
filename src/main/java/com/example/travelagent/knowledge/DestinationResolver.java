package com.example.travelagent.knowledge;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DestinationResolver {

    private static final String COMMON_KEY = "common";

    private final Map<String, String> aliases = Map.ofEntries(
            Map.entry("四川", "sichuan"),
            Map.entry("成都", "sichuan"),
            Map.entry("川西", "sichuan"),
            Map.entry("九寨沟", "sichuan"),
            Map.entry("都江堰", "sichuan"),
            Map.entry("青城山", "sichuan"),
            Map.entry("乐山", "sichuan"),
            Map.entry("峨眉山", "sichuan"),

            Map.entry("云南", "yunnan"),
            Map.entry("昆明", "yunnan"),
            Map.entry("大理", "yunnan"),
            Map.entry("丽江", "yunnan"),
            Map.entry("香格里拉", "yunnan"),
            Map.entry("西双版纳", "yunnan"),

            Map.entry("广东", "guangdong"),
            Map.entry("广州", "guangdong"),
            Map.entry("深圳", "guangdong"),
            Map.entry("珠海", "guangdong"),
            Map.entry("佛山", "guangdong"),
            Map.entry("东莞", "guangdong"),
            Map.entry("惠州", "guangdong"),
            Map.entry("汕头", "guangdong"),
            Map.entry("潮汕", "guangdong"),
            Map.entry("珠三角", "guangdong"),
            Map.entry("大湾区", "guangdong")
    );

    public String resolve(String destination) {
        if (destination == null || destination.isBlank()) {
            return COMMON_KEY;
        }

        String trimmed = destination.trim();
        return aliases.entrySet()
                .stream()
                .filter(entry -> trimmed.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(COMMON_KEY);
    }

    public boolean hasDedicatedKnowledgeBase(String destinationKey) {
        return !COMMON_KEY.equals(destinationKey);
    }
}
