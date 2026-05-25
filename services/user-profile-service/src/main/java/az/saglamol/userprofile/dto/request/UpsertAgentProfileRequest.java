package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import az.saglamol.userprofile.entity.ProfileStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpsertAgentProfileRequest(
        @NotNull UUID insuranceCompanyId,
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Size(max = 120) String lastName,
        @NotBlank @Size(max = 120) String employeeCode,
        @Size(max = 160) String department,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$") @Size(max = 32) String phone,
        @NotBlank @Email @Size(max = 320) String email,
        ProfileStatus profileStatus
) {
}
