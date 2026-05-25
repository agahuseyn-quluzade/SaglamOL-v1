package az.saglamol.common.events.claim;

import java.time.Instant;
import java.util.UUID;

public record ClaimRejectedEvent(
        UUID claimId,
        UUID companyId,
        UUID patientProfileId,
        String reason,
        String decidedBy,
        Instant occurredAt
) {
}
