package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record AgentLinkedToCompanyEvent(
        UUID agentProfileId,
        UUID companyId,
        Instant occurredAt
) {
}
