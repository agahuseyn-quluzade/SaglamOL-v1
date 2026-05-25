package az.saglamol.common.events.healthrecord;

import java.time.Instant;
import java.util.UUID;

public record MedicalDocumentConfirmedEvent(
        UUID documentId,
        UUID healthRecordId,
        UUID patientProfileId,
        String sha256Hash,
        Instant occurredAt
) {
}
