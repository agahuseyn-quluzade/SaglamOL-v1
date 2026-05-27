package az.saglamol.airisk.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimDetailResponse(
        UUID id,
        String claimNumber,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        UUID doctorProfileId,
        String status,
        String serviceType,
        LocalDate treatmentDate,
        BigDecimal claimAmount
) {
}
