package com.alphatragen.notification.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        List<FieldErrorResponse> errors
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true,
                "SUCCESS", "요청을 처리했습니다.",
                data, null);
    }

}
