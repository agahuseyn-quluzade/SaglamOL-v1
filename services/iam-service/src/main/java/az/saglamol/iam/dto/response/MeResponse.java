package az.saglamol.iam.dto.response;

import java.util.List;
import java.util.UUID;

public record MeResponse(
        UUID userId,
        String email,
        String phoneNumber,
        List<String> roles
) {
}
