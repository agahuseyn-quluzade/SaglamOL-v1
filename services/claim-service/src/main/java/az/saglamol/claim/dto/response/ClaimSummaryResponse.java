package az.saglamol.claim.dto.response;

import az.saglamol.claim.entity.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimSummaryResponse(
        UUID id,
        String claimNumber,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        ClaimStatus status,
        String serviceType,
        LocalDate treatmentDate,
        BigDecimal claimAmount,
        BigDecimal coveredAmount,
        BigDecimal patientPayAmount
) {
}
