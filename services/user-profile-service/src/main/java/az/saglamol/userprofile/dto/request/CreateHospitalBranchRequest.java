package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHospitalBranchRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 255) String addressLine,
        @Size(max = 80) String phone
) {
}
