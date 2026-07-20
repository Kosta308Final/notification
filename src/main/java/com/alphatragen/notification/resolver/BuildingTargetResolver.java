package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BuildingTargetResolver implements TargetResolver {

    private final UserServiceClient userServiceClient;

    public BuildingTargetResolver(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean supports(NotificationTargetType targetType) {
        return targetType == NotificationTargetType.BUILDING;
    }

    @Override
    public List<Long> resolve(Long apartmentId, Long userId, String building, String unit, String role) {
        if (building == null || building.isBlank()) {
            throw new IllegalArgumentException("building is required for BUILDING target type");
        }
        return userServiceClient.findUsersByBuilding(apartmentId, building);
    }
}
