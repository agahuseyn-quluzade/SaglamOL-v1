package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Component
public class SuspiciousTimingRule implements FraudRule {
    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        if (context.policyStartDate() == null || context.treatmentDate() == null) {
            return FraudRuleResult.none();
        }
        long days = ChronoUnit.DAYS.between(context.policyStartDate(), context.treatmentDate());
        if (days < 0 || days > 7) {
            return FraudRuleResult.none();
        }
        return new FraudRuleResult(
                FraudSignalType.SUSPICIOUS_TIMING,
                FraudSignalSeverity.MEDIUM,
                new BigDecimal("0.15"),
                "Claim occurred within 7 days of policy start"
        );
    }
}
