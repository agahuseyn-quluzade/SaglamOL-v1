package az.saglamol.healthrecord.dto.response;

import az.saglamol.healthrecord.entity.DocumentType;
import az.saglamol.healthrecord.entity.MedicalDocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record MedicalDocumentResponse(
        UUID id,
        UUID healthRecordId,
        UUID treatmentId,
        UUID claimId,
        UUID patientProfileId,
        UUID hospitalId,
        UUID uploadedByUserId,
        DocumentType documentType,
        String fileName,
        Long fileSize,
        String contentType,
        String storageBucket,
        String minioKey,
        String sha256Hash,
        MedicalDocumentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
