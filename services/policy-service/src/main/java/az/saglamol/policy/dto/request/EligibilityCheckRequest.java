package az.saglamol.policy.dto.request;

import az.saglamol.policy.entity.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EligibilityCheckRequest(
        @NotNull UUID policyId,
        @NotNull UUID insuranceCompanyId,
        @NotNull UUID patientProfileId,
        UUID hospitalId,
        @NotNull ServiceType serviceType,
        @NotNull @DecimalMin("0.01") BigDecimal claimAmount,
        @NotNull LocalDate treatmentDate
) {
}
