package az.saglamol.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneLoginRequest(
        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phoneNumber must contain 7 to 15 digits and may start with +")
        String phoneNumber,
        @NotBlank String password
) {
}
