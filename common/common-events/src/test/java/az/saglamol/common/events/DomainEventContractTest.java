package az.saglamol.common.events;

import az.saglamol.common.events.claim.ClaimSubmittedEvent;
import az.saglamol.common.events.payment.PaymentCompletedEvent;
import az.saglamol.common.events.policy.PolicyCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DomainEventContractTest {

    @Test
    void exposesKafkaTopicConstants() {
        assertEquals("iam.events", KafkaTopics.IAM_EVENTS);
        assertEquals("profile.events", KafkaTopics.PROFILE_EVENTS);
        assertEquals("policy.events", KafkaTopics.POLICY_EVENTS);
        assertEquals("payment.events", KafkaTopics.PAYMENT_EVENTS);
        assertEquals("claim.events", KafkaTopics.CLAIM_EVENTS);
        assertEquals("health-record.events", KafkaTopics.HEALTH_RECORD_EVENTS);
        assertEquals("risk.events", KafkaTopics.RISK_EVENTS);
        assertEquals("fraud.events", KafkaTopics.FRAUD_EVENTS);
        assertEquals("notification.events", KafkaTopics.NOTIFICATION_EVENTS);
        assertEquals("claim.events.dlt", KafkaTopics.CLAIM_EVENTS_DLT);
    }

    @Test
    void keepsClaimSubmittedEventFieldContract() {
        UUID claimId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        ClaimSubmittedEvent event = new ClaimSubmittedEvent(
                claimId,
                "CLM-0001",
                UUID.randomUUID(),
                companyId,
                patientProfileId,
                null,
                null,
                "INPATIENT",
                new BigDecimal("380.75"),
                List.of(documentId),
                Instant.parse("2026-05-25T11:00:00Z")
        );

        assertEquals(claimId, event.claimId());
        assertEquals(companyId, event.companyId());
        assertEquals(patientProfileId, event.patientProfileId());
        assertEquals(documentId, event.documentIds().getFirst());
        assertNull(event.hospitalId());
        assertNull(event.doctorProfileId());
    }

    @Test
    void keepsPaymentCompletedEventFieldContract() {
        UUID paymentId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                paymentId,
                "PAY-1001",
                companyId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("99.90"),
                "AZN",
                "PREMIUM",
                Instant.parse("2026-05-25T12:00:00Z")
        );

        assertEquals(paymentId, event.paymentId());
        assertEquals(companyId, event.companyId());
        assertEquals("AZN", event.currency());
        assertNull(event.hospitalId());
    }

    @Test
    void keepsPolicyCreatedEventFieldContract() {
        UUID companyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        UUID agentProfileId = UUID.randomUUID();
        BigDecimal premium = new BigDecimal("145.00");

        PolicyCreatedEvent event = new PolicyCreatedEvent(
                UUID.randomUUID(),
                "POL-9001",
                companyId,
                UUID.randomUUID(),
                patientProfileId,
                agentProfileId,
                premium,
                Instant.parse("2026-05-25T13:00:00Z")
        );

        assertEquals(companyId, event.companyId());
        assertEquals(patientProfileId, event.patientProfileId());
        assertEquals(agentProfileId, event.agentProfileId());
        assertEquals(0, premium.compareTo(event.premiumAmount()));
    }
}
