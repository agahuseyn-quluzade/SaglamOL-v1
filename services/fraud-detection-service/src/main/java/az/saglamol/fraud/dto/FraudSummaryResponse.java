package az.saglamol.fraud.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FraudSummaryResponse(
        UUID scopeId,
        long totalAssessments,
        long completedAssessments,
        long pendingAssessments,
        long failedAssessments,
        BigDecimal averageScore
) {
}
