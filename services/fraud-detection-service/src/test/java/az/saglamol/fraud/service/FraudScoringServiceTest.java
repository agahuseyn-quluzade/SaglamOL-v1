package az.saglamol.fraud.service;

import az.saglamol.fraud.entity.FraudLevel;
import az.saglamol.fraud.entity.FraudSignalType;
import az.saglamol.fraud.rule.DoctorAnomalyRule;
import az.saglamol.fraud.rule.DuplicateDocumentRule;
import az.saglamol.fraud.rule.FraudContext;
import az.saglamol.fraud.rule.FraudRule;
import az.saglamol.fraud.rule.FrequentClaimsRule;
import az.saglamol.fraud.rule.HighAmountRule;
import az.saglamol.fraud.rule.HospitalAnomalyRule;
import az.saglamol.fraud.rule.SuspiciousTimingRule;
import az.saglamol.fraud.rule.WaitingPeriodRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudScoringServiceTest {

    @Test
    void eachFraudRuleProducesExpectedSignal() {
        assertSignal(new DuplicateDocumentRule(), context(true, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 3),
                FraudSignalType.DUPLICATE_DOCUMENT);
        assertSignal(new FrequentClaimsRule(), context(false, 3, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 3),
                FraudSignalType.FREQUENT_CLAIMS);
        assertSignal(new HighAmountRule(), context(false, 0, new BigDecimal("301.00"), new BigDecimal("100.00"), 0, 0, 3),
                FraudSignalType.HIGH_AMOUNT);
        assertSignal(new SuspiciousTimingRule(), context(false, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 3),
                FraudSignalType.SUSPICIOUS_TIMING);
        assertSignal(new WaitingPeriodRule(), context(false, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 20),
                FraudSignalType.WAITING_PERIOD_VIOLATION);
        assertSignal(new HospitalAnomalyRule(), context(false, 0, BigDecimal.ZERO, BigDecimal.ZERO, 3, 0, 40),
                FraudSignalType.HOSPITAL_ANOMALY);
        assertSignal(new DoctorAnomalyRule(), context(false, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 3, 40),
                FraudSignalType.DOCTOR_ANOMALY);
    }

    @Test
    void combinedScoreIsCappedAndCriticalRequiresManualReview() {
        FraudScoringService service = new FraudScoringService(List.of(
                new DuplicateDocumentRule(),
                new FrequentClaimsRule(),
                new HighAmountRule(),
                new SuspiciousTimingRule(),
                new WaitingPeriodRule(),
                new HospitalAnomalyRule(),
                new DoctorAnomalyRule()
        ));

        var result = service.score(context(true, 3, new BigDecimal("1000.00"), new BigDecimal("100.00"), 3, 3, 3));

        assertEquals(new BigDecimal("1.0000"), result.fraudScore());
        assertEquals(FraudLevel.CRITICAL, result.fraudLevel());
        assertTrue(result.manualReviewRequired());
        assertEquals(7, result.signals().size());
    }

    @Test
    void lowScoreDoesNotRequireManualReview() {
        FraudScoringService service = new FraudScoringService(List.of(new HospitalAnomalyRule()));

        var result = service.score(context(false, 0, BigDecimal.ZERO, BigDecimal.ZERO, 3, 0, 40));

        assertEquals(new BigDecimal("0.1500"), result.fraudScore());
        assertEquals(FraudLevel.LOW, result.fraudLevel());
        assertFalse(result.manualReviewRequired());
    }

    private void assertSignal(FraudRule rule, FraudContext context, FraudSignalType type) {
        var result = rule.evaluate(context);

        assertEquals(type, result.signalType());
    }

    private FraudContext context(boolean duplicate, long recentClaims, BigDecimal claimAmount, BigDecimal average,
                                 long hospitalHighRisk, long doctorHighRisk, int daysAfterPolicyStart) {
        UUID hospitalId = hospitalHighRisk > 0 ? UUID.randomUUID() : null;
        UUID doctorId = doctorHighRisk > 0 ? UUID.randomUUID() : null;
        return new FraudContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                hospitalId,
                doctorId,
                claimAmount,
                LocalDate.now(),
                LocalDate.now().minusDays(daysAfterPolicyStart),
                recentClaims,
                average,
                hospitalHighRisk,
                doctorHighRisk,
                List.of("a".repeat(64)),
                duplicate
        );
    }
}
