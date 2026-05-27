package az.saglamol.healthrecord.dto.response;

import java.util.UUID;

public record MedicalDocumentHashResponse(
        UUID documentId,
        String sha256Hash,
        UUID patientProfileId,
        UUID claimId,
        UUID hospitalId
) {
}
