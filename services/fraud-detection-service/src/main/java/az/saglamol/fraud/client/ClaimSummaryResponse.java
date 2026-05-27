package az.saglamol.fraud.client;

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
        String status,
        String serviceType,
        LocalDate treatmentDate,
        BigDecimal claimAmount,
        BigDecimal coveredAmount,
        BigDecimal patientPayAmount
) {
}
