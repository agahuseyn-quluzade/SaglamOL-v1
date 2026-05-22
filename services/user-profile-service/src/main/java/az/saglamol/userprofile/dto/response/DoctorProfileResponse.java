package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DoctorProfileResponse(
        UUID id,
        UUID userId,
        String licenseNo,
        UUID hospitalId,
        String specialty,
        Instant createdAt
) {
}
