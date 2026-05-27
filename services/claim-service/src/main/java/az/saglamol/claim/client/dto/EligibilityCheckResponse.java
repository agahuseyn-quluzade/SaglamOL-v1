package az.saglamol.claim.client.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EligibilityCheckResponse(
        boolean eligible,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        String serviceType,
        BigDecimal claimAmount,
        BigDecimal coveredAmount,
        BigDecimal availableLimit,
        Integer coveragePercent,
        boolean inNetwork,
        List<String> reasons
) {
}
