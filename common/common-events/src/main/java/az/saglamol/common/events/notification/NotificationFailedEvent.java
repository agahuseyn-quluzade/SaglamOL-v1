package az.saglamol.common.events.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationFailedEvent(
        UUID notificationId,
        UUID recipientUserId,
        String channel,
        String reason,
        Instant occurredAt
) {
}
