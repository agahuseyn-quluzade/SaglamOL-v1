package az.saglamol.common.events.notification;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationRequestedEvent(
        UUID recipientUserId,
        String channel,
        String templateCode,
        Map<String, Object> variables,
        String relatedEntityType,
        UUID relatedEntityId,
        UUID companyId,
        UUID hospitalId,
        Instant occurredAt
) {
}
