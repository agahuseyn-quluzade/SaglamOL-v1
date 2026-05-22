package az.saglamol.iam.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String status
) {
}
