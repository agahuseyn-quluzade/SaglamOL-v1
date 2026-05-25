package az.saglamol.userprofile.dto.request;

import az.saglamol.userprofile.entity.InsuranceCompanyStaffRoleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateInsuranceCompanyStaffRequest(
        @NotNull UUID iamUserId,
        @NotNull InsuranceCompanyStaffRoleType roleType,
        @Size(max = 120) String position,
        @Size(max = 120) String employeeCode
) {
}
