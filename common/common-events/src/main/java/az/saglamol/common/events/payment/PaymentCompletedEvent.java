package az.saglamol.common.events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID paymentId,
        String paymentNumber,
        UUID companyId,
        UUID policyId,
        UUID claimId,
        UUID patientProfileId,
        UUID hospitalId,
        BigDecimal amount,
        String currency,
        String paymentType,
        Instant occurredAt
) {
}
