package az.saglamol.common.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimApprovedEvent(
        UUID claimId,
        UUID companyId,
        UUID patientProfileId,
        UUID policyId,
        BigDecimal approvedAmount,
        String decidedBy,
        Instant occurredAt
) {
}
