package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HouseholdTargetResolver implements TargetResolver {

    private final UserServiceClient userServiceClient;

    public HouseholdTargetResolver(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean supports(NotificationTargetType targetType) {
        return targetType == NotificationTargetType.HOUSEHOLD;
    }

    @Override
    public List<Long> resolve(TargetCondition condition) {
        String building = condition.building();
        String unit = condition.unit();
        if (building == null || building.isBlank() || unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("building and unit are required for HOUSEHOLD target type");
        }
        return userServiceClient.findUsersByHousehold(condition.apartmentId(), building, unit);
    }
}
