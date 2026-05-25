package az.saglamol.policy.dto.request;

import az.saglamol.policy.entity.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCoverageRuleRequest(
        @NotNull ServiceType serviceType,
        @NotNull @Min(0) @Max(100) Integer coveragePercent,
        @NotNull @DecimalMin("0.00") BigDecimal maxAmount,
        @NotNull @Min(0) Integer waitingPeriodDays,
        boolean requiresPreApproval
) {
}
