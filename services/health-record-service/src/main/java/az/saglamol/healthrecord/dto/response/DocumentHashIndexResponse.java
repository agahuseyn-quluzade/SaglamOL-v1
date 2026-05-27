package az.saglamol.healthrecord.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DocumentHashIndexResponse(
        UUID id,
        String sha256Hash,
        UUID documentId,
        UUID patientProfileId,
        UUID claimId,
        UUID hospitalId,
        Instant createdAt
) {
}
