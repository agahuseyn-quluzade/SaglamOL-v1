package az.saglamol.common.events.policy;

import java.time.Instant;
import java.util.UUID;

public record PolicyLimitReleasedEvent(
        UUID reservationId,
        UUID policyId,
        UUID claimId,
        String reason,
        Instant occurredAt
) {
}
