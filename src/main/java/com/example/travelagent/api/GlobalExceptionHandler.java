package com.example.travelagent.api;

import com.example.travelagent.application.AiResponseParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * 全局异常处理器。
 *
 * 作用：
 * 统一捕获 Controller 层抛出的异常，并返回统一格式 ApiResponse。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 AI 返回内容解析失败。
     *
     * 例如：
     * 大模型没有返回合法 JSON，导致 ObjectMapper 解析失败。
     */
    @ExceptionHandler(AiResponseParseException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiResponse<Void> handleAiResponseParseException(AiResponseParseException exception) {
        log.warn("AI response parse failed.", exception);
        return ApiResponse.fail(
                50201,
                "AI 返回的旅行计划格式不正确，请稍后重试。"
        );
    }

    /**
     * 处理请求参数校验失败。
     *
     * 例如：
     * days 小于 1，departureCity 为空。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("请求参数校验失败。");

        return ApiResponse.fail(
                40001,
                message
        );
    }

    /**
     * 兜底异常处理。
     *
     * 防止未预料的异常直接把堆栈暴露给前端。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("Unhandled API exception.", exception);
        return ApiResponse.fail(
                50001,
                "系统内部异常，请稍后重试。"
        );
    }
}
