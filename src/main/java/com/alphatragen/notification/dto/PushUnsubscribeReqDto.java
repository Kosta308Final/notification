package com.alphatragen.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record PushUnsubscribeReqDto(
        @NotBlank(message = "endpoint is required") String endpoint
) {
}
