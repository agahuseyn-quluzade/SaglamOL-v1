package az.saglamol.userprofile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateInsuranceCompanyRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 80) String taxId,
        @NotBlank @Size(max = 120) String licenseNumber,
        @Email @Size(max = 320) String email,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$") @Size(max = 32) String phone,
        @Valid AddressRequest address
) {
}
