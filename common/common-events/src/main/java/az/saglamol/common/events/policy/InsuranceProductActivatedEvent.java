package az.saglamol.common.events.policy;

import java.time.Instant;
import java.util.UUID;

public record InsuranceProductActivatedEvent(
        UUID productId,
        UUID companyId,
        Instant occurredAt
) {
}
