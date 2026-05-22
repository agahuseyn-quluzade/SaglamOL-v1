package az.saglamol.iam.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String phoneNumber,
        String status,
        Instant createdAt,
        Instant lastLoginAt,
        List<String> roles,
        List<String> permissions
) {
}
