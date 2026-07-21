package com.alphatragen.notification.resolver;

import com.alphatragen.notification.config.NotificationProperties;
import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Component
@Primary
public class BackendUserServiceClient implements UserServiceClient {

    private static final String TOKEN_HEADER = "X-Notification-Internal-Token";

    private final RestClient restClient;
    private final String internalToken;

    public BackendUserServiceClient(
            RestClient.Builder restClientBuilder,
            NotificationProperties properties) {
        NotificationProperties.Backend backend = properties.backend();
        this.restClient = restClientBuilder.baseUrl(backend.baseUrl()).build();
        this.internalToken = backend.internalToken();
    }

    @Override
    public List<Long> findUsersByIndividual(Long apartmentId, Long userId) {
        return find(new TargetCondition(NotificationTargetType.INDIVIDUAL, apartmentId, userId, null, null, null));
    }

    @Override
    public List<Long> findUsersByHousehold(Long apartmentId, String building, String unit) {
        return find(new TargetCondition(NotificationTargetType.HOUSEHOLD, apartmentId, null, building, unit, null));
    }

    @Override
    public List<Long> findUsersByBuilding(Long apartmentId, String building) {
        return find(new TargetCondition(NotificationTargetType.BUILDING, apartmentId, null, building, null, null));
    }

    @Override
    public List<Long> findUsersByRole(Long apartmentId, String role) {
        return find(new TargetCondition(NotificationTargetType.ROLE, apartmentId, null, null, null, role));
    }

    @Override
    public List<Long> findUsersByApartment(Long apartmentId) {
        return find(new TargetCondition(NotificationTargetType.APARTMENT, apartmentId, null, null, null, null));
    }

    private List<Long> find(TargetCondition condition) {
        BackendApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/internal/notification-recipients")
                        .queryParam("apartmentId", condition.apartmentId())
                        .queryParam("targetType", condition.targetType())
                        .queryParamIfPresent("userId", java.util.Optional.ofNullable(condition.userId()))
                        .queryParamIfPresent("building", java.util.Optional.ofNullable(condition.building()))
                        .queryParamIfPresent("unit", java.util.Optional.ofNullable(condition.unit()))
                        .queryParamIfPresent("role", java.util.Optional.ofNullable(condition.role()))
                        .build())
                .header(TOKEN_HEADER, internalToken)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<BackendApiResponse>() {});

        if (response == null || !response.success()) {
            String code = response == null ? "EMPTY_RESPONSE" : response.code();
            String message = response == null ? "backend returned an empty response" : response.message();
            throw new IllegalStateException("Failed to resolve notification recipients: " + code + " " + message);
        }
        return Objects.requireNonNullElse(response.data(), List.of());
    }

    private record BackendApiResponse(
            boolean success,
            String code,
            String message,
            List<Long> data) {
    }
}
