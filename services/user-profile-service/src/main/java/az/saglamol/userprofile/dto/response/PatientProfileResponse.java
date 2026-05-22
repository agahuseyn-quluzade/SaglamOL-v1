package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientProfileResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        Instant createdAt
) {
}
