package az.saglamol.policy.dto.response;

import az.saglamol.policy.entity.CoverageType;
import az.saglamol.policy.entity.InsuranceProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InsuranceProductResponse(
        UUID id,
        UUID insuranceCompanyId,
        String productCode,
        String name,
        String description,
        CoverageType coverageType,
        BigDecimal premiumAmount,
        BigDecimal annualLimit,
        String currency,
        InsuranceProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
