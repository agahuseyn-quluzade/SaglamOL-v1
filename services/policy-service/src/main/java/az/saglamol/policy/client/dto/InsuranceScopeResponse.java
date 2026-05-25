package az.saglamol.policy.client.dto;

import java.util.List;
import java.util.UUID;

public record InsuranceScopeResponse(
        UUID iamUserId,
        UUID insuranceCompanyId,
        List<String> roles,
        boolean canView,
        boolean canManage,
        boolean isAgent,
        boolean isInsuranceAdmin,
        boolean isInsuranceStaff
) {
}
