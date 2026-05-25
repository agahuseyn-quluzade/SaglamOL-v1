package az.saglamol.common.events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimPayoutRequestedEvent(
        UUID paymentId,
        UUID claimId,
        UUID companyId,
        UUID patientProfileId,
        UUID hospitalId,
        BigDecimal amount,
        Instant occurredAt
) {
}
