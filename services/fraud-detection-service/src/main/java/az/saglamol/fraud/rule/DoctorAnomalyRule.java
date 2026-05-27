package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DoctorAnomalyRule implements FraudRule {
    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        if (context.doctorProfileId() == null || context.doctorHighRiskAssessments() < 3) {
            return FraudRuleResult.none();
        }
        return new FraudRuleResult(
                FraudSignalType.DOCTOR_ANOMALY,
                FraudSignalSeverity.MEDIUM,
                new BigDecimal("0.15"),
                "Doctor has repeated high-risk assessments in the recent window"
        );
    }
}
