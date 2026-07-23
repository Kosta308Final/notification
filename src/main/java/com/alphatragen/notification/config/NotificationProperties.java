package com.alphatragen.notification.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record NotificationProperties(
        @Valid Backend backend,
        @Valid Vapid vapid,
        @Valid Kafka kafka,
        @Valid Cors cors) {

    public record Backend(
            @NotBlank String baseUrl,
            @NotBlank String internalToken) {
    }

    public record Vapid(
            @NotBlank(message = "VAPID configurations (public-key, private-key, subject) must not be empty") String publicKey,
            @NotBlank(message = "VAPID configurations (public-key, private-key, subject) must not be empty") String privateKey,
            @NotBlank(message = "VAPID configurations (public-key, private-key, subject) must not be empty") String subject) {
    }

    public record Kafka(@Min(0) long retryBackoffMs) {
    }

    public record Cors(@NotEmpty List<@NotBlank String> allowedOrigins) {
    }
}
