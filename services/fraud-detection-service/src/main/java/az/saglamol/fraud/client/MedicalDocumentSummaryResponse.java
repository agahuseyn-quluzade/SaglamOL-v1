package az.saglamol.fraud.client;

import java.util.UUID;

public record MedicalDocumentSummaryResponse(
        UUID id,
        UUID healthRecordId,
        UUID treatmentId,
        UUID claimId,
        UUID patientProfileId,
        UUID hospitalId,
        String documentType,
        String fileName,
        Long fileSize,
        String contentType,
        String status
) {
}
