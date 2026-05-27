package az.saglamol.fraud.rule;

public interface FraudRule {
    FraudRuleResult evaluate(FraudContext context);
}
