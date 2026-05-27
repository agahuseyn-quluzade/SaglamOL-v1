package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FrequentClaimsRule implements FraudRule {
    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        if (context.patientClaimsLast30Days() < 3) {
            return FraudRuleResult.none();
        }
        return new FraudRuleResult(
                FraudSignalType.FREQUENT_CLAIMS,
                FraudSignalSeverity.MEDIUM,
                new BigDecimal("0.20"),
                "Patient has 3 or more claims in the last 30 days"
        );
    }
}
