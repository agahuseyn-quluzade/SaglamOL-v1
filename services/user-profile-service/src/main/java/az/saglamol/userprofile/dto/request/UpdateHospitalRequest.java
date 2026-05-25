package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateHospitalRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 80) String phone,
        @Email @Size(max = 320) String email
) {
}
