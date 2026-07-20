package com.alphatragen.notification.response;

public record FieldErrorResponse(
        String field,
        String reason
) {
}
