package az.saglamol.userprofile.dto.response;

import az.saglamol.userprofile.entity.InsuranceCompanyStaffRoleType;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffStatus;

import java.time.Instant;
import java.util.UUID;

public record InsuranceCompanyStaffResponse(
        UUID id,
        UUID iamUserId,
        UUID insuranceCompanyId,
        InsuranceCompanyStaffRoleType roleType,
        String position,
        String employeeCode,
        InsuranceCompanyStaffStatus status,
        Instant createdAt
) {
}
