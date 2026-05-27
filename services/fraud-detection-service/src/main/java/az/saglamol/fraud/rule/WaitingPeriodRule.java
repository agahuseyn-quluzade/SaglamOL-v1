package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Component
public class WaitingPeriodRule implements FraudRule {
    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        if (context.policyStartDate() == null || context.treatmentDate() == null) {
            return FraudRuleResult.none();
        }
        long days = ChronoUnit.DAYS.between(context.policyStartDate(), context.treatmentDate());
        if (days < 0 || days >= 30) {
            return FraudRuleResult.none();
        }
        return new FraudRuleResult(
                FraudSignalType.WAITING_PERIOD_VIOLATION,
                FraudSignalSeverity.HIGH,
                new BigDecimal("0.20"),
                "Claim falls inside the default 30 day waiting period"
        );
    }
}
