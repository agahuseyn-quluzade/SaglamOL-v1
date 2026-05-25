package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record HospitalCreatedEvent(
        UUID hospitalId,
        String name,
        Instant occurredAt
) {
}
