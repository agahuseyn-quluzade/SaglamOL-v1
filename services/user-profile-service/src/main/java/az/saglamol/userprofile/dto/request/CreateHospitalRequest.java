package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHospitalRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 80) String taxId,
        @NotBlank @Size(max = 120) String licenseNo,
        @Size(max = 80) String phone,
        @Email @Size(max = 320) String email
) {
}
