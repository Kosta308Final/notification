package com.alphatragen.notification.template;

import com.alphatragen.notification.domain.NotificationEventType;
import java.util.Map;

public interface NotificationTemplateService {
    TemplateResult generate(NotificationEventType eventType, Map<String, Object> templateData);
}
