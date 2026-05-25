package az.saglamol.common.events.policy;

import java.time.Instant;
import java.util.UUID;

public record PolicyLimitConfirmedEvent(
        UUID reservationId,
        UUID policyId,
        UUID claimId,
        Instant occurredAt
) {
}
