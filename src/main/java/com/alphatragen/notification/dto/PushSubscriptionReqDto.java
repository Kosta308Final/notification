package com.alphatragen.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record PushSubscriptionReqDto(
        @NotBlank(message = "endpoint is required") String endpoint,
        @NotBlank(message = "p256dh is required") String p256dh,
        @NotBlank(message = "auth is required") String auth,
        String browser,
        String deviceType
) {
}
