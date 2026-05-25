package az.saglamol.common.events.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PolicyPaymentPendingEvent(
        UUID policyId,
        UUID companyId,
        UUID patientProfileId,
        BigDecimal premiumAmount,
        Instant occurredAt
) {
}
