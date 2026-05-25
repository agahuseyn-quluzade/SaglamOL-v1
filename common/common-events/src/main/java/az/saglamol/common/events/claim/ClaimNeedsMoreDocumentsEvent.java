package az.saglamol.common.events.claim;

import java.time.Instant;
import java.util.UUID;

public record ClaimNeedsMoreDocumentsEvent(
        UUID claimId,
        UUID companyId,
        UUID patientProfileId,
        String reason,
        Instant occurredAt
) {
}
