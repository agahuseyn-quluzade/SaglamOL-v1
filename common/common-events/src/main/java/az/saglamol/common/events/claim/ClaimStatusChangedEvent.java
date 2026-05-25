package az.saglamol.common.events.claim;

import java.time.Instant;
import java.util.UUID;

public record ClaimStatusChangedEvent(
        UUID claimId,
        UUID companyId,
        String oldStatus,
        String newStatus,
        String reason,
        Instant occurredAt
) {
}
