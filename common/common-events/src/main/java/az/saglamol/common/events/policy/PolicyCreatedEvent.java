package az.saglamol.common.events.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PolicyCreatedEvent(
        UUID policyId,
        String policyNumber,
        UUID companyId,
        UUID productId,
        UUID patientProfileId,
        UUID agentProfileId,
        BigDecimal premiumAmount,
        Instant occurredAt
) {
}
