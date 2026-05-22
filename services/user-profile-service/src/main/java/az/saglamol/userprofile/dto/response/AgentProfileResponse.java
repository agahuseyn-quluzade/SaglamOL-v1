package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;
import az.saglamol.userprofile.entity.ProfileStatus;

public record AgentProfileResponse(
        UUID id,
        UUID iamUserId,
        String firstName,
        String lastName,
        String employeeCode,
        String department,
        String phone,
        String email,
        ProfileStatus profileStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
