package az.saglamol.policy.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LimitReservationRequest(
        @NotNull UUID claimId,
        @NotNull UUID companyId,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
