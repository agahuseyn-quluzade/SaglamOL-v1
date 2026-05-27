package az.saglamol.notification.dto;

import az.saglamol.notification.entity.NotificationChannel;
import az.saglamol.notification.entity.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientUserId,
        String recipientEmail,
        String recipientPhone,
        NotificationChannel channel,
        String templateCode,
        String subject,
        String message,
        NotificationStatus status,
        int retryCount,
        int maxRetries,
        String relatedEntityType,
        UUID relatedEntityId,
        UUID insuranceCompanyId,
        UUID hospitalId,
        Instant createdAt,
        Instant sentAt,
        Instant updatedAt,
        String errorMessage
) {
}
