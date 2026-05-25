package az.saglamol.policy.dto.request;

import az.saglamol.policy.entity.PolicyStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyRequest(
        @NotBlank @Size(max = 80) String policyNumber,
        @NotNull UUID insuranceCompanyId,
        @NotNull UUID productId,
        @NotNull UUID patientProfileId,
        UUID agentProfileId,
        @NotNull PolicyStatus status,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.00") BigDecimal premiumAmount,
        @NotNull @DecimalMin("0.00") BigDecimal annualLimit,
        @NotNull @DecimalMin("0.00") BigDecimal usedLimit,
        @NotNull @DecimalMin("0.00") BigDecimal reservedLimit
) {
}
