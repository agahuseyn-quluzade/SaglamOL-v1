package az.saglamol.common.events.healthrecord;

import java.time.Instant;
import java.util.UUID;

public record MedicalDocumentUploadedEvent(
        UUID documentId,
        UUID healthRecordId,
        UUID patientProfileId,
        Instant occurredAt
) {
}
