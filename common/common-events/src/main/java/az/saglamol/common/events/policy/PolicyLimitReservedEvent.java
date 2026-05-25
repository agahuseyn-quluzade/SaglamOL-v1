package az.saglamol.common.events.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PolicyLimitReservedEvent(
        UUID reservationId,
        UUID policyId,
        UUID companyId,
        UUID claimId,
        BigDecimal reservedAmount,
        Instant occurredAt
) {
}
