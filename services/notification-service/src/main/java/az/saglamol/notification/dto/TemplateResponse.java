package az.saglamol.notification.dto;

import az.saglamol.notification.entity.NotificationChannel;
import az.saglamol.notification.entity.TemplateStatus;

import java.time.Instant;
import java.util.UUID;

public record TemplateResponse(
        UUID id,
        String templateCode,
        NotificationChannel channel,
        String subjectTemplate,
        String bodyTemplate,
        TemplateStatus status,
        Instant createdAt
) {
}
