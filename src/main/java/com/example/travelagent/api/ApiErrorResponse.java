package com.example.travelagent.api;

public record ApiErrorResponse(
        String code,
        String message
) {
}