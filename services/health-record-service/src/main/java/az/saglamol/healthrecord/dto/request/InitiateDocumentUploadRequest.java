package az.saglamol.healthrecord.dto.request;

import az.saglamol.healthrecord.entity.DocumentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InitiateDocumentUploadRequest(
        UUID treatmentId,
        UUID claimId,
        @NotNull UUID patientProfileId,
        UUID hospitalId,
        @NotNull DocumentType documentType,
        @NotBlank String fileName,
        @NotNull @Min(0) Long fileSize,
        @NotBlank String contentType
) {
}
