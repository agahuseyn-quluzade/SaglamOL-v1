package az.saglamol.common.events.policy;

import java.time.Instant;
import java.util.UUID;

public record PolicyCancelledEvent(
        UUID policyId,
        UUID companyId,
        UUID patientProfileId,
        String reason,
        Instant occurredAt
) {
}
