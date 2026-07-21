package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.beans.factory.annotation.Value;
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
            @Value("${app.backend.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.backend.internal-token:local-notification-service-token}") String internalToken) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }

    @Override
    public List<Long> findUsersByIndividual(Long apartmentId, Long userId) {
        return find(apartmentId, NotificationTargetType.INDIVIDUAL, userId, null, null, null);
    }

    @Override
    public List<Long> findUsersByHousehold(Long apartmentId, String building, String unit) {
        return find(apartmentId, NotificationTargetType.HOUSEHOLD, null, building, unit, null);
    }

    @Override
    public List<Long> findUsersByBuilding(Long apartmentId, String building) {
        return find(apartmentId, NotificationTargetType.BUILDING, null, building, null, null);
    }

    @Override
    public List<Long> findUsersByRole(Long apartmentId, String role) {
        return find(apartmentId, NotificationTargetType.ROLE, null, null, null, role);
    }

    @Override
    public List<Long> findUsersByApartment(Long apartmentId) {
        return find(apartmentId, NotificationTargetType.APARTMENT, null, null, null, null);
    }

    private List<Long> find(
            Long apartmentId,
            NotificationTargetType targetType,
            Long userId,
            String building,
            String unit,
            String role) {
        BackendApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/internal/notification-recipients")
                        .queryParam("apartmentId", apartmentId)
                        .queryParam("targetType", targetType)
                        .queryParamIfPresent("userId", java.util.Optional.ofNullable(userId))
                        .queryParamIfPresent("building", java.util.Optional.ofNullable(building))
                        .queryParamIfPresent("unit", java.util.Optional.ofNullable(unit))
                        .queryParamIfPresent("role", java.util.Optional.ofNullable(role))
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
