package az.saglamol.iam.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phoneNumber must contain 7 to 15 digits and may start with +")
        String phoneNumber,
        @NotBlank @Size(min = 8, max = 128) String password
) {
}
