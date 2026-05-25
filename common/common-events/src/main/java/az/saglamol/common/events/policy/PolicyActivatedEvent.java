package az.saglamol.common.events.policy;

import java.time.Instant;
import java.util.UUID;

public record PolicyActivatedEvent(
        UUID policyId,
        UUID companyId,
        UUID patientProfileId,
        Instant occurredAt
) {
}
