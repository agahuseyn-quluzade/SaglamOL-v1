package az.saglamol.healthrecord.dto.response;

import az.saglamol.healthrecord.entity.DocumentType;
import az.saglamol.healthrecord.entity.MedicalDocumentStatus;

import java.util.UUID;

public record MedicalDocumentSummaryResponse(
        UUID id,
        UUID healthRecordId,
        UUID treatmentId,
        UUID claimId,
        UUID patientProfileId,
        UUID hospitalId,
        DocumentType documentType,
        String fileName,
        Long fileSize,
        String contentType,
        MedicalDocumentStatus status
) {
}
