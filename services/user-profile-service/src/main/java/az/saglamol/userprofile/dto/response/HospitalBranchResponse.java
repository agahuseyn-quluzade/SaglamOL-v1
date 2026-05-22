package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;

public record HospitalBranchResponse(
        UUID id,
        UUID hospitalId,
        String name,
        String city,
        String addressLine,
        String phone,
        Instant createdAt
) {
}
