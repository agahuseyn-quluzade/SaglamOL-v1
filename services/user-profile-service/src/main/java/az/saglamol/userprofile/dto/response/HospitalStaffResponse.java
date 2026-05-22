package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;

public record HospitalStaffResponse(
        UUID id,
        UUID userId,
        UUID hospitalId,
        UUID branchId,
        String position,
        Instant createdAt
) {
}
