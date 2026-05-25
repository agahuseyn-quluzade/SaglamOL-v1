package az.saglamol.common.events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimPayoutCompletedEvent(
        UUID paymentId,
        UUID claimId,
        UUID companyId,
        BigDecimal paidAmount,
        Instant occurredAt
) {
}
