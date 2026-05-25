package az.saglamol.common.events.fraud;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudCheckCompletedEvent(
        UUID claimId,
        UUID companyId,
        UUID fraudCheckId,
        Double fraudScore,
        String fraudLevel,
        List<String> signals,
        boolean passed,
        Instant occurredAt
) {
}
