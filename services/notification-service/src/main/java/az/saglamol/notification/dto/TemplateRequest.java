package az.saglamol.notification.dto;

import az.saglamol.notification.entity.NotificationChannel;

public record TemplateRequest(
        String templateCode,
        NotificationChannel channel,
        String subjectTemplate,
        String bodyTemplate
) {
}
