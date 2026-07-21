package com.alphatragen.notification.template;

import com.alphatragen.notification.domain.NotificationEventType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    @Override
    public TemplateResult generate(NotificationEventType eventType, Map<String, Object> templateData) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }

        NotificationTemplate template = NotificationTemplate.from(eventType);
        validateRequired(templateData, template);

        return new TemplateResult(
                replacePlaceholders(template.getTitleTemplate(), templateData, template),
                replacePlaceholders(template.getContentTemplate(), templateData, template)
        );
    }

    private void validateRequired(Map<String, Object> templateData, NotificationTemplate template) {
        if (templateData == null) {
            throw new IllegalArgumentException("Template data cannot be null");
        }
        for (String key : template.getRequiredKeys()) {
            if (!templateData.containsKey(key)
                    || templateData.get(key) == null
                    || String.valueOf(templateData.get(key)).trim().isEmpty()) {
                throw new IllegalArgumentException("Missing required template variable: " + key);
            }
        }
    }

    private String replacePlaceholders(
            String templateText,
            Map<String, Object> templateData,
            NotificationTemplate template
    ) {
        String result = templateText;
        for (String key : template.getRequiredKeys()) {
            result = result.replace("{" + key + "}", String.valueOf(templateData.get(key)));
        }
        return result;
    }
}
