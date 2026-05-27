package az.saglamol.fraud.rule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FraudContext(
        UUID claimId,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        UUID doctorProfileId,
        BigDecimal claimAmount,
        LocalDate treatmentDate,
        LocalDate policyStartDate,
        long patientClaimsLast30Days,
        BigDecimal averageCompanyClaimAmount,
        long hospitalHighRiskAssessments,
        long doctorHighRiskAssessments,
        List<String> documentHashes,
        boolean hasDuplicateDocument
) {
}
