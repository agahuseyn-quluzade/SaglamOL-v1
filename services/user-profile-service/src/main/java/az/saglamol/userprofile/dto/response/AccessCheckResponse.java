package az.saglamol.userprofile.dto.response;

import java.util.UUID;

public record AccessCheckResponse(
        UUID iamUserId,
        UUID companyId,
        UUID insuranceCompanyId,
        boolean canView,
        boolean canManage,
        boolean canManageStaff,
        boolean canAgentOperate
) {
}
