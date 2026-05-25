package az.saglamol.common.events.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID paymentId,
        UUID companyId,
        UUID policyId,
        String reason,
        Instant occurredAt
) {
}
