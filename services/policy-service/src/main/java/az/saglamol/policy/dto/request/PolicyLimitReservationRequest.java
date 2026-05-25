package az.saglamol.policy.dto.request;

import az.saglamol.policy.entity.ReservationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PolicyLimitReservationRequest(
        @NotNull UUID policyId,
        @NotNull UUID insuranceCompanyId,
        @NotNull UUID claimId,
        @NotNull @DecimalMin("0.00") BigDecimal reservedAmount,
        @NotNull ReservationStatus status
) {
}
