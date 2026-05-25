package az.saglamol.common.events.risk;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskAnalysisCompletedEvent(
        UUID claimId,
        UUID companyId,
        UUID riskAnalysisId,
        Double score,
        String level,
        Double confidence,
        List<String> reasons,
        Instant occurredAt
) {
}
