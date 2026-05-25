package az.saglamol.common.events.iam;

import java.time.Instant;
import java.util.UUID;

public record UserStatusChangedEvent(
        UUID userId,
        String oldStatus,
        String newStatus,
        Instant occurredAt
) {
}
