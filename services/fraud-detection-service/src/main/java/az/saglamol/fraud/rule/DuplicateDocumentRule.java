package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DuplicateDocumentRule implements FraudRule {
    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        if (!context.hasDuplicateDocument()) {
            return FraudRuleResult.none();
        }
        return new FraudRuleResult(
                FraudSignalType.DUPLICATE_DOCUMENT,
                FraudSignalSeverity.HIGH,
                new BigDecimal("0.35"),
                "One or more claim documents duplicate an existing document hash"
        );
    }
}
