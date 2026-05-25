package az.saglamol.common.events.iam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String email,
        String phoneNumber,
        List<String> roles,
        Instant occurredAt
) {
}
