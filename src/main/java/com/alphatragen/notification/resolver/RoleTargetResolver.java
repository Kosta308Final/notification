package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleTargetResolver implements TargetResolver {

    private final UserServiceClient userServiceClient;

    public RoleTargetResolver(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean supports(NotificationTargetType targetType) {
        return targetType == NotificationTargetType.ROLE;
    }

    @Override
    public List<Long> resolve(TargetCondition condition) {
        String role = condition.role();
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role is required for ROLE target type");
        }
        return userServiceClient.findUsersByRole(condition.apartmentId(), role);
    }
}
