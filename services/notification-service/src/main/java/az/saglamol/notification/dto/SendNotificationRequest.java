package az.saglamol.notification.dto;

import az.saglamol.notification.entity.NotificationChannel;

import java.util.Map;
import java.util.UUID;

public record SendNotificationRequest(
        UUID recipientUserId,
        String recipientEmail,
        String recipientPhone,
        NotificationChannel channel,
        String templateCode,
        Map<String, Object> variables,
        String relatedEntityType,
        UUID relatedEntityId,
        UUID insuranceCompanyId,
        UUID hospitalId
) {
}
