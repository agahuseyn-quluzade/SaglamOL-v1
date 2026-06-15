package az.saglamol.common.security;

import java.util.List;
import java.util.UUID;

public record JwtPrincipal(
        UUID userId,
        String email,
        String phoneNumber,
        List<String> roles
) {
}
