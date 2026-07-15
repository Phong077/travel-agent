package com.example.travelagent.api;

/**
 * 统一接口响应结构。
 *
 * @param code 业务状态码，0 表示成功
 * @param message 提示信息
 * @param data 响应数据
 */
public record ApiResponse<T>(
        int code,
        String message,
        T data
) {

    /**
     * 成功响应。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 失败响应。
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}