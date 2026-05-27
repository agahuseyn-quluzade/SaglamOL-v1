package az.saglamol.fraud.dto;

import az.saglamol.fraud.entity.FraudAssessmentStatus;
import az.saglamol.fraud.entity.FraudLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FraudAssessmentResponse(
        UUID id,
        UUID claimId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        UUID doctorProfileId,
        BigDecimal fraudScore,
        FraudLevel fraudLevel,
        boolean manualReviewRequired,
        FraudAssessmentStatus status,
        Instant createdAt,
        Instant completedAt
) {
}
