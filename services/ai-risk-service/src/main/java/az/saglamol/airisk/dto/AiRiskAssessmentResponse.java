package az.saglamol.airisk.dto;

import az.saglamol.airisk.entity.AiRiskAssessmentStatus;
import az.saglamol.airisk.entity.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiRiskAssessmentResponse(
        UUID id,
        UUID claimId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID policyId,
        UUID hospitalId,
        UUID doctorProfileId,
        BigDecimal riskScore,
        RiskLevel riskLevel,
        BigDecimal confidence,
        List<String> reasons,
        String rawProviderResponse,
        AiRiskAssessmentStatus status,
        Instant createdAt,
        Instant completedAt
) {
}
