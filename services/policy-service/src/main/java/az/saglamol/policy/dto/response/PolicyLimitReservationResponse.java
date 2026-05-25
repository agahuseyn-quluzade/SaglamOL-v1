package az.saglamol.policy.dto.response;

import az.saglamol.policy.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PolicyLimitReservationResponse(
        UUID id,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID claimId,
        BigDecimal reservedAmount,
        ReservationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
