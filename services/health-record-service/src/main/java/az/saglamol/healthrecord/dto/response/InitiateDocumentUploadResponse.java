package az.saglamol.healthrecord.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InitiateDocumentUploadResponse(
        UUID documentId,
        String uploadUrl,
        String storageBucket,
        String minioKey,
        Instant expiresAt
) {
}
