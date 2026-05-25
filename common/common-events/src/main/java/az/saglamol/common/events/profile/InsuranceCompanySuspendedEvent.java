package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record InsuranceCompanySuspendedEvent(
        UUID companyId,
        String reason,
        Instant occurredAt
) {
}
