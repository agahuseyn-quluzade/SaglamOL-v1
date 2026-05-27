package az.saglamol.claim.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PolicyLimitReservationResponse(
        UUID id,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID claimId,
        BigDecimal reservedAmount,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
