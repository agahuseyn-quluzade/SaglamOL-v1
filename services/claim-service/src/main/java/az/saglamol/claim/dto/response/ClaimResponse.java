package az.saglamol.claim.dto.response;

import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.PayoutRecipientType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimResponse(
        UUID id,
        String claimNumber,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        UUID doctorProfileId,
        ClaimStatus status,
        String serviceType,
        LocalDate treatmentDate,
        BigDecimal claimAmount,
        BigDecimal approvedAmount,
        BigDecimal coveredAmount,
        BigDecimal patientPayAmount,
        PayoutRecipientType payoutRecipientType,
        UUID policyReservationId,
        String diagnosisCode,
        String reason,
        String notes,
        Integer riskScore,
        String riskLevel,
        Integer fraudScore,
        String fraudLevel,
        Boolean fraudPassed,
        Instant createdAt,
        Instant updatedAt
) {
}
