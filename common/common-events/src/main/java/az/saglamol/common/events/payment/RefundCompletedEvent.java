package az.saglamol.common.events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundCompletedEvent(
        UUID paymentId,
        UUID companyId,
        BigDecimal refundedAmount,
        Instant occurredAt
) {
}
