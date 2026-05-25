package az.saglamol.common.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimPaidEvent(
        UUID claimId,
        UUID companyId,
        UUID patientProfileId,
        BigDecimal paidAmount,
        Instant occurredAt
) {
}
