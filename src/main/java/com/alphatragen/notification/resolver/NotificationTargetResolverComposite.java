package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationTargetResolverComposite {

    private final Map<NotificationTargetType, TargetResolver> resolverMap;

    public NotificationTargetResolverComposite(List<TargetResolver> resolvers) {
        this.resolverMap = new EnumMap<>(NotificationTargetType.class);
        for (NotificationTargetType targetType : NotificationTargetType.values()) {
            TargetResolver resolver = findResolver(resolvers, targetType);
            if (resolver != null) {
                resolverMap.put(targetType, resolver);
            }
        }
    }

    public List<Long> resolveTargets(TargetCondition condition) {
        TargetResolver resolver = resolverMap.get(condition.targetType());
        if (resolver == null) {
            throw new IllegalArgumentException("Unsupported target type: " + condition.targetType());
        }

        List<Long> resolved = resolver.resolve(condition);

        // Ensure distinct userIds (deduplication) and no nulls
        return resolved.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private TargetResolver findResolver(List<TargetResolver> resolvers, NotificationTargetType targetType) {
        return resolvers.stream()
                .filter(r -> r.supports(targetType))
                .findFirst()
                .orElse(null);
    }
}
