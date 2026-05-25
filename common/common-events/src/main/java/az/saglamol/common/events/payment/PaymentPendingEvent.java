package az.saglamol.common.events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentPendingEvent(
        UUID paymentId,
        String paymentNumber,
        UUID companyId,
        UUID policyId,
        UUID patientProfileId,
        BigDecimal amount,
        String currency,
        String paymentType,
        Instant occurredAt
) {
}
