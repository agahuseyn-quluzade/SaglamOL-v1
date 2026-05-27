package az.saglamol.healthrecord.dto.response;

import java.time.Instant;
import java.util.UUID;

public record HealthAccessLogResponse(
        UUID id,
        UUID healthRecordId,
        UUID documentId,
        UUID accessedByUserId,
        String accessRole,
        String reason,
        Instant accessedAt
) {
}
