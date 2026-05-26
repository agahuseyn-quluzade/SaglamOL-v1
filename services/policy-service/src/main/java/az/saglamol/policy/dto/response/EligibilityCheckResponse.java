package az.saglamol.policy.dto.response;

import az.saglamol.policy.entity.ServiceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EligibilityCheckResponse(
        boolean eligible,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID hospitalId,
        ServiceType serviceType,
        BigDecimal claimAmount,
        BigDecimal coveredAmount,
        BigDecimal availableLimit,
        Integer coveragePercent,
        boolean inNetwork,
        List<String> reasons
) {
}
