package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;

public record HospitalResponse(
        UUID id,
        String name,
        String taxId,
        String licenseNo,
        String phone,
        String email,
        String status,
        Instant createdAt
) {
}
