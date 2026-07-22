package com.alphatragen.notification.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record NotificationProperties(
        @Valid Backend backend,
        @Valid Vapid vapid,
        @Valid Kafka kafka,
        @Valid Cors cors) {

    public NotificationProperties {
        if (backend == null) {
            backend = new Backend("http://localhost:8080", "local-notification-service-token");
        }
        if (vapid == null) {
            vapid = new Vapid(null, null, null);
        }
        if (kafka == null) {
            kafka = new Kafka(1000);
        }
        if (cors == null) {
            cors = new Cors(List.of("http://localhost:5173", "http://localhost:3000"));
        }
    }

    public record Backend(
            @NotBlank String baseUrl,
            @NotBlank String internalToken) {
    }

    public record Vapid(
            @NotBlank(message = "VAPID configurations (public-key, private-key, subject) must not be empty") String publicKey,
            @NotBlank(message = "VAPID configurations (public-key, private-key, subject) must not be empty") String privateKey,
            @NotBlank(message = "VAPID configurations (public-key, private-key, subject) must not be empty") String subject) {

        public Vapid {
            if (!StringUtils.hasText(publicKey) || !StringUtils.hasText(privateKey) || !StringUtils.hasText(subject)) {
                throw new IllegalStateException("VAPID configurations (public-key, private-key, subject) must not be empty");
            }
        }
    }

    public record Kafka(@Min(0) long retryBackoffMs) {
    }

    public record Cors(@NotEmpty List<@NotBlank String> allowedOrigins) {
    }
}
