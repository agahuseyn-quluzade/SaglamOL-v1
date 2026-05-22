package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpsertDoctorProfileRequest(
        @NotBlank @Size(max = 120) String licenseNo,
        UUID hospitalId,
        @Size(max = 160) String specialty
) {
}
