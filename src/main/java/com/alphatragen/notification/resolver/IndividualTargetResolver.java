package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndividualTargetResolver implements TargetResolver {

    private final UserServiceClient userServiceClient;

    public IndividualTargetResolver(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean supports(NotificationTargetType targetType) {
        return targetType == NotificationTargetType.INDIVIDUAL;
    }

    @Override
    public List<Long> resolve(TargetCondition condition) {
        Long userId = condition.userId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required for INDIVIDUAL target type");
        }
        return userServiceClient.findUsersByIndividual(condition.apartmentId(), userId);
    }
}
