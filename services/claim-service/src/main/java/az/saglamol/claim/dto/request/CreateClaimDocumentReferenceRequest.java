package az.saglamol.claim.dto.request;

import az.saglamol.claim.entity.ClaimDocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateClaimDocumentReferenceRequest(
        @NotNull UUID documentId,
        @NotBlank String documentType,
        boolean required,
        @NotNull ClaimDocumentStatus status
) {
}
