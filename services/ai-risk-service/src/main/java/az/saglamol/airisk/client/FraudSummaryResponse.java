package az.saglamol.airisk.client;

import java.math.BigDecimal;
import java.util.UUID;

public record FraudSummaryResponse(
        UUID scopeId,
        long totalAssessments,
        long completedCount,
        long pendingCount,
        long failedCount,
        BigDecimal averageFraudScore
) {
}
