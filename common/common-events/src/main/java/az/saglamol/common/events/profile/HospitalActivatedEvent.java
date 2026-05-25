package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record HospitalActivatedEvent(
        UUID hospitalId,
        Instant occurredAt
) {
}
