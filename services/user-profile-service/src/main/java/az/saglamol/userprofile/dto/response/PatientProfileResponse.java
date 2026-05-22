package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import az.saglamol.userprofile.entity.Gender;
import az.saglamol.userprofile.entity.ProfileStatus;

public record PatientProfileResponse(
        UUID id,
        UUID iamUserId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        String phone,
        String email,
        String nationalId,
        AddressResponse address,
        String emergencyContactName,
        String emergencyContactPhone,
        ProfileStatus profileStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
