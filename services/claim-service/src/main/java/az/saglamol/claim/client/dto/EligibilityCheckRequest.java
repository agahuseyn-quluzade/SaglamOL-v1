package az.saglamol.claim.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EligibilityCheckRequest(
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        String serviceType,
        BigDecimal claimAmount,
        LocalDate treatmentDate
) {
}
