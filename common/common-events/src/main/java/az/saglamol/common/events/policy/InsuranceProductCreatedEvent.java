package az.saglamol.common.events.policy;

import java.time.Instant;
import java.util.UUID;

public record InsuranceProductCreatedEvent(
        UUID productId,
        UUID companyId,
        String productCode,
        String name,
        Instant occurredAt
) {
}
