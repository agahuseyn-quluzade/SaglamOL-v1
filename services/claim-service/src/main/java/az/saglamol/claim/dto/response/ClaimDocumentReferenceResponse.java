package az.saglamol.claim.dto.response;

import az.saglamol.claim.entity.ClaimDocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record ClaimDocumentReferenceResponse(
        UUID id,
        UUID claimId,
        UUID documentId,
        String documentType,
        boolean required,
        ClaimDocumentStatus status,
        Instant createdAt
) {
}
