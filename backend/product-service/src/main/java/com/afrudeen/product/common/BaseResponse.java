package com.afrudeen.product.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponse<T>(
        boolean success,
        int status,
        String message,
        T data,
        Map<String, String> errors,
        Instant timestamp,
        String path) {

    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(true, 200, "Success", data, null, Instant.now(), null);
    }

    public static <T> BaseResponse<T> ok(String message, T data) {
        return new BaseResponse<>(true, 200, message, data, null, Instant.now(), null);
    }

    public static <T> BaseResponse<T> created(T data) {
        return new BaseResponse<>(true, 201, "Created", data, null, Instant.now(), null);
    }

    public static <T> BaseResponse<T> error(int status, String message,
                                            String path, Map<String, String> errors) {
        return new BaseResponse<>(false, status, message, null, errors, Instant.now(), path);
    }
}