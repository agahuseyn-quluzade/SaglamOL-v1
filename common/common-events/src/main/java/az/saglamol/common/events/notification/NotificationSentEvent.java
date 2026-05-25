package az.saglamol.common.events.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationSentEvent(
        UUID notificationId,
        UUID recipientUserId,
        String channel,
        Instant occurredAt
) {
}
