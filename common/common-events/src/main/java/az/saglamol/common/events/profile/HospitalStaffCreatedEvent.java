package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record HospitalStaffCreatedEvent(
        UUID hospitalId,
        UUID staffProfileId,
        UUID iamUserId,
        Instant occurredAt
) {
}
