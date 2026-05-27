package az.saglamol.airisk.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AiRiskSummaryResponse(
        UUID companyId,
        long totalAssessments,
        long successCount,
        long fallbackCount,
        long failedCount,
        BigDecimal averageRiskScore
) {
}
