package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;

import java.math.BigDecimal;

public record FraudRuleResult(
        FraudSignalType signalType,
        FraudSignalSeverity severity,
        BigDecimal scoreImpact,
        String message
) {
    public static FraudRuleResult none() {
        return null;
    }
}
