package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AgentProfileResponse(
        UUID id,
        UUID userId,
        String employeeNo,
        String department,
        Instant createdAt
) {
}
