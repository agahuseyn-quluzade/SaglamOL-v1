package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record InsuranceCompanyCreatedEvent(
        UUID companyId,
        String name,
        String taxId,
        Instant occurredAt
) {
}
