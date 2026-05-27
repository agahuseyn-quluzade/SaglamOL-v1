package az.saglamol.healthrecord.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmDocumentUploadRequest(
        @NotBlank String sha256Hash
) {
}
