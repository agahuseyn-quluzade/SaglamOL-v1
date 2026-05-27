package az.saglamol.airisk.model;

import az.saglamol.airisk.entity.AiRiskAssessmentStatus;
import az.saglamol.airisk.entity.RiskLevel;

import java.math.BigDecimal;
import java.util.List;

public record RiskModelResult(
        BigDecimal riskScore,
        RiskLevel riskLevel,
        BigDecimal confidence,
        List<String> reasons,
        String rawProviderResponse,
        AiRiskAssessmentStatus status,
        String providerName,
        String model,
        String responsePayload,
        String errorMessage
) {
}
