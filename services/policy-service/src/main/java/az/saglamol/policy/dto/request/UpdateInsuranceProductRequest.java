package az.saglamol.policy.dto.request;

import az.saglamol.policy.entity.CoverageType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateInsuranceProductRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 1000) String description,
        @NotNull CoverageType coverageType,
        @NotNull @DecimalMin("0.00") BigDecimal premiumAmount,
        @NotNull @DecimalMin("0.00") BigDecimal annualLimit,
        @Size(min = 3, max = 3) String currency
) {
}
