package az.saglamol.claim.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyDetailResponse(
        UUID id,
        String policyNumber,
        UUID insuranceCompanyId,
        UUID productId,
        UUID patientProfileId,
        UUID agentProfileId,
        String status,
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
