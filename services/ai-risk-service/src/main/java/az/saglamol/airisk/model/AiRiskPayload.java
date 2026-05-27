package az.saglamol.airisk.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AiRiskPayload(
        UUID claimId,
        UUID companyId,
        UUID policyId,
        UUID hospitalId,
        UUID doctorProfileId,
        String serviceType,
        LocalDate treatmentDate,
        BigDecimal claimAmount,
        String claimStatus,
        PolicyContext policy,
        FraudContext fraud
) {
    public record PolicyContext(
            String status,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal annualLimit,
            BigDecimal usedLimit,
            BigDecimal reservedLimit
    ) {
    }

    public record FraudContext(
            BigDecimal averageFraudScore,
            long completedAssessments,
            long failedAssessments
    ) {
    }
}
