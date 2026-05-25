package az.saglamol.policy.dto.response;

import az.saglamol.policy.entity.PolicyStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyResponse(
        UUID id,
        String policyNumber,
        UUID insuranceCompanyId,
        UUID productId,
        UUID patientProfileId,
        UUID agentProfileId,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal premiumAmount,
        BigDecimal annualLimit,
        BigDecimal usedLimit,
        BigDecimal reservedLimit,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
