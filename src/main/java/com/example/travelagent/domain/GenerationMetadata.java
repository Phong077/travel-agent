package com.example.travelagent.domain;

public record GenerationMetadata(
        String mode,
        int attempts,
        boolean validated
) {
}
