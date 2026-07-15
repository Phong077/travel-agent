package com.example.travelagent.application;

//AI 返回的内容解析失败时抛出的异常。
public class AiResponseParseException extends RuntimeException {

    public AiResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }
}