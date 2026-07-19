package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationTargetResolverComposite {

    private final List<TargetResolver> resolvers;

    public NotificationTargetResolverComposite(List<TargetResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public List<Long> resolveTargets(NotificationTargetType targetType, Long apartmentId, Long userId, String building, String unit, String role) {
        TargetResolver resolver = resolvers.stream()
                .filter(r -> r.supports(targetType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported target type: " + targetType));

        List<Long> resolved = resolver.resolve(apartmentId, userId, building, unit, role);

        // Ensure distinct userIds (deduplication) and no nulls
        return resolved.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
