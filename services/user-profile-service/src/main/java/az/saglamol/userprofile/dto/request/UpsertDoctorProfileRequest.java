package az.saglamol.userprofile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import az.saglamol.userprofile.entity.ProfileStatus;

public record UpsertDoctorProfileRequest(
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Size(max = 120) String lastName,
        @NotBlank @Size(max = 160) String specialty,
        @NotBlank @Size(max = 120) String licenseNumber,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$") @Size(max = 32) String phone,
        @NotBlank @Email @Size(max = 320) String email,
        @Valid AddressRequest address,
        ProfileStatus profileStatus
) {
}
