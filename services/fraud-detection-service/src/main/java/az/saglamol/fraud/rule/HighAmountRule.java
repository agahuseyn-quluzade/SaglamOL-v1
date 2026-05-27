package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {
    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        if (context.averageCompanyClaimAmount() == null
                || context.averageCompanyClaimAmount().compareTo(BigDecimal.ZERO) <= 0
                || context.claimAmount() == null) {
            return FraudRuleResult.none();
        }
        if (context.claimAmount().compareTo(context.averageCompanyClaimAmount().multiply(new BigDecimal("3"))) <= 0) {
            return FraudRuleResult.none();
        }
        return new FraudRuleResult(
                FraudSignalType.HIGH_AMOUNT,
                FraudSignalSeverity.HIGH,
                new BigDecimal("0.25"),
                "Claim amount exceeds 3x the company average"
        );
    }
}
