package az.saglamol.common.events.payment;

import java.time.Instant;
import java.util.UUID;

public record ClaimPayoutFailedEvent(
        UUID paymentId,
        UUID claimId,
        UUID companyId,
        String reason,
        Instant occurredAt
) {
}
