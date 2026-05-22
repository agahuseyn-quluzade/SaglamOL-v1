package az.saglamol.iam.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RevokeRefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
