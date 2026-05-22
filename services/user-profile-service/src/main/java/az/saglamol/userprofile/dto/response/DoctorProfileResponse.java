package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;
import az.saglamol.userprofile.entity.ProfileStatus;

public record DoctorProfileResponse(
        UUID id,
        UUID iamUserId,
        String firstName,
        String lastName,
        String specialty,
        String licenseNumber,
        String phone,
        String email,
        AddressResponse address,
        ProfileStatus profileStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
