package az.saglamol.fraud.rule;

import az.saglamol.fraud.entity.FraudSignalType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FraudRulesTest {

    @Test
    void duplicateDocumentRuleCreatesSignal() {
        var result = new DuplicateDocumentRule().evaluate(context(true, 0, new BigDecimal("100.00"), new BigDecimal("100.00")));

        assertNotNull(result);
        assertEquals(FraudSignalType.DUPLICATE_DOCUMENT, result.signalType());
    }

    @Test
    void frequentClaimsRuleCreatesSignalForThreeOrMoreClaims() {
        var result = new FrequentClaimsRule().evaluate(context(false, 3, new BigDecimal("100.00"), new BigDecimal("100.00")));

        assertNotNull(result);
        assertEquals(FraudSignalType.FREQUENT_CLAIMS, result.signalType());
    }

    @Test
    void highAmountRuleTriggersWhenClaimExceedsThreeTimesAverage() {
        var result = new HighAmountRule().evaluate(context(false, 0, new BigDecimal("301.00"), new BigDecimal("100.00")));

        assertNotNull(result);
        assertEquals(FraudSignalType.HIGH_AMOUNT, result.signalType());
    }

    @Test
    void highAmountRuleDoesNotTriggerWithoutAverage() {
        var result = new HighAmountRule().evaluate(context(false, 0, new BigDecimal("301.00"), BigDecimal.ZERO));

        assertNull(result);
    }

    @Test
    void suspiciousTimingRuleTriggersWithinSevenDays() {
        FraudContext context = context(false, 0, new BigDecimal("100.00"), new BigDecimal("100.00"));

        var result = new SuspiciousTimingRule().evaluate(context);

        assertNotNull(result);
        assertEquals(FraudSignalType.SUSPICIOUS_TIMING, result.signalType());
    }

    private FraudContext context(boolean duplicate, long recentClaims, BigDecimal claimAmount, BigDecimal average) {
        return new FraudContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                claimAmount,
                LocalDate.now(),
                LocalDate.now().minusDays(3),
                recentClaims,
                average,
                0,
                0,
                List.of("a".repeat(64)),
                duplicate
        );
    }
}
