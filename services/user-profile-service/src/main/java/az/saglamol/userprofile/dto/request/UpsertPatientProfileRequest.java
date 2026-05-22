package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.time.LocalDate;
import az.saglamol.userprofile.entity.Gender;
import az.saglamol.userprofile.entity.ProfileStatus;

public record UpsertPatientProfileRequest(
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Size(max = 120) String lastName,
        @PastOrPresent LocalDate dateOfBirth,
        Gender gender,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$") @Size(max = 32) String phone,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 80) String nationalId,
        @Valid AddressRequest address,
        @Size(max = 160) String emergencyContactName,
        @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$") @Size(max = 32) String emergencyContactPhone,
        ProfileStatus profileStatus
) {
}
