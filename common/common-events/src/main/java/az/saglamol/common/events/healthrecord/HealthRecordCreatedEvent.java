package az.saglamol.common.events.healthrecord;

import java.time.Instant;
import java.util.UUID;

public record HealthRecordCreatedEvent(
        UUID healthRecordId,
        UUID patientProfileId,
        UUID doctorProfileId,
        UUID hospitalId,
        Instant occurredAt
) {
}
