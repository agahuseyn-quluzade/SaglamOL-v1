package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 120) String country,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 120) String district,
        @NotBlank @Size(max = 255) String street,
        @Size(max = 32) String postalCode
) {
}
