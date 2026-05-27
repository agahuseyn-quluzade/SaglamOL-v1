package az.saglamol.airisk.service;

import az.saglamol.airisk.config.AiRiskProperties;
import az.saglamol.airisk.entity.AiRiskAssessmentStatus;
import az.saglamol.airisk.entity.RiskLevel;
import az.saglamol.airisk.model.AiRiskPayload;
import az.saglamol.airisk.model.RiskModelResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class FallbackRiskModelClient implements RiskModelClient {

    private final AiRiskProperties properties;

    public FallbackRiskModelClient(AiRiskProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskModelResult assess(AiRiskPayload payload) {
        BigDecimal score = fallbackScore(payload);
        RiskLevel level = level(score);
        List<String> reasons = reasons(payload, score);
        return new RiskModelResult(
                score,
                level,
                new BigDecimal("0.5500"),
                reasons,
                "{\"fallback\":true}",
                AiRiskAssessmentStatus.FALLBACK_USED,
                "fallback",
                properties.getModelName(),
                "{\"fallback\":true,\"riskScore\":" + score + ",\"riskLevel\":\"" + level + "\"}",
                null
        );
    }

    private BigDecimal fallbackScore(AiRiskPayload payload) {
        BigDecimal score = new BigDecimal("0.1000");
        if (payload.claimAmount() != null && payload.claimAmount().compareTo(new BigDecimal("1000.00")) > 0) {
            score = score.add(new BigDecimal("0.2500"));
        }
        if (payload.policy() != null && payload.policy().annualLimit() != null && payload.claimAmount() != null
                && payload.claimAmount().compareTo(payload.policy().annualLimit().multiply(new BigDecimal("0.5"))) > 0) {
            score = score.add(new BigDecimal("0.2500"));
        }
        if (payload.fraud() != null && payload.fraud().averageFraudScore() != null) {
            score = score.add(payload.fraud().averageFraudScore().min(new BigDecimal("0.3000")));
        }
        return score.min(BigDecimal.ONE).setScale(4, RoundingMode.HALF_UP);
    }

    private List<String> reasons(AiRiskPayload payload, BigDecimal score) {
        List<String> reasons = new ArrayList<>();
        reasons.add("Fallback rules used because external AI was unavailable or disabled");
        if (payload.claimAmount() != null && payload.claimAmount().compareTo(new BigDecimal("1000.00")) > 0) {
            reasons.add("Claim amount is above fallback high amount threshold");
        }
        if (payload.fraud() != null && payload.fraud().averageFraudScore() != null && payload.fraud().averageFraudScore().signum() > 0) {
            reasons.add("Company fraud history contributes to risk");
        }
        if (reasons.size() == 1 && score.compareTo(new BigDecimal("0.2500")) < 0) {
            reasons.add("No major fallback risk indicators detected");
        }
        return reasons;
    }

    private RiskLevel level(BigDecimal score) {
        if (score.compareTo(new BigDecimal("0.8000")) >= 0) {
            return RiskLevel.CRITICAL;
        }
        if (score.compareTo(new BigDecimal("0.5000")) >= 0) {
            return RiskLevel.HIGH;
        }
        if (score.compareTo(new BigDecimal("0.2500")) >= 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
