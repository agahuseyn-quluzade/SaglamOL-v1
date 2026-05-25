package az.saglamol.common.events.risk;

import java.time.Instant;
import java.util.UUID;

public record RiskAnalysisRequestedEvent(
        UUID claimId,
        UUID companyId,
        UUID patientProfileId,
        UUID policyId,
        Instant occurredAt
) {
}
