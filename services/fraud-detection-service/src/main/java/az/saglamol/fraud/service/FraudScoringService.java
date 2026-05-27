package az.saglamol.fraud.service;

import az.saglamol.fraud.entity.FraudLevel;
import az.saglamol.fraud.rule.FraudContext;
import az.saglamol.fraud.rule.FraudRule;
import az.saglamol.fraud.rule.FraudRuleResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FraudScoringService {

    private final List<FraudRule> rules;

    public FraudScoringService(List<FraudRule> rules) {
        this.rules = rules;
    }

    public FraudScoringResult score(FraudContext context) {
        List<FraudRuleResult> signals = rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(result -> result != null)
                .toList();
        BigDecimal score = signals.stream()
                .map(FraudRuleResult::scoreImpact)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .min(BigDecimal.ONE)
                .setScale(4, RoundingMode.HALF_UP);
        FraudLevel level = level(score);
        return new FraudScoringResult(score, level, level == FraudLevel.HIGH || level == FraudLevel.CRITICAL, signals);
    }

    private FraudLevel level(BigDecimal score) {
        if (score.compareTo(new BigDecimal("0.80")) >= 0) {
            return FraudLevel.CRITICAL;
        }
        if (score.compareTo(new BigDecimal("0.50")) >= 0) {
            return FraudLevel.HIGH;
        }
        if (score.compareTo(new BigDecimal("0.25")) >= 0) {
            return FraudLevel.MEDIUM;
        }
        return FraudLevel.LOW;
    }

    public record FraudScoringResult(
            BigDecimal fraudScore,
            FraudLevel fraudLevel,
            boolean manualReviewRequired,
            List<FraudRuleResult> signals
    ) {
    }
}
