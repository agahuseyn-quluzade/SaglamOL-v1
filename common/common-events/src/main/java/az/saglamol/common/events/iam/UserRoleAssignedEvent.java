package az.saglamol.common.events.iam;

import java.time.Instant;
import java.util.UUID;

public record UserRoleAssignedEvent(
        UUID userId,
        String roleName,
        Instant occurredAt
) {
}
