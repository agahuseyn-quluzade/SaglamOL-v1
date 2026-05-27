package az.saglamol.claim.event.consumer;

import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.PayoutRecipientType;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.ClaimStatusHistoryRepository;
import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.fraud.FraudCheckCompletedEvent;
import az.saglamol.common.events.payment.ClaimPayoutCompletedEvent;
import az.saglamol.common.events.risk.RiskAnalysisCompletedEvent;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimKafkaConsumerIdempotencyTest {

    @Mock
    private ProcessedEventService processedEventService;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimStatusHistoryRepository historyRepository;
    @Mock
    private OutboxEventService<OutboxEvent> outboxEventService;

    private ObjectMapper objectMapper;
    private UUID claimId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        claimId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void duplicateRiskEventIsSkipped() {
        EventEnvelope<RiskAnalysisCompletedEvent> envelope = riskEnvelope(UUID.randomUUID());
        when(processedEventService.isAlreadyProcessed(
                envelope.eventId(),
                "claim-service-risk-analysis-consumer"
        )).thenReturn(true);

        new RiskAnalysisConsumer(processedEventService, claimRepository, historyRepository, objectMapper)
                .onMessage(envelope);

        verify(claimRepository, never()).findById(any());
        verify(processedEventService, never()).markProcessed(any(), any(), any());
    }

    @Test
    void riskConsumerUpdatesClaimAndMarksProcessed() {
        EventEnvelope<RiskAnalysisCompletedEvent> envelope = riskEnvelope(UUID.randomUUID());
        Claim claim = claim(ClaimStatus.UNDER_REVIEW);
        when(processedEventService.isAlreadyProcessed(
                envelope.eventId(),
                "claim-service-risk-analysis-consumer"
        )).thenReturn(false);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        new RiskAnalysisConsumer(processedEventService, claimRepository, historyRepository, objectMapper)
                .onMessage(envelope);

        assertEquals(82, claim.getRiskScore());
        assertEquals("HIGH", claim.getRiskLevel());
        verify(claimRepository).save(claim);
        verify(processedEventService).markProcessed(
                envelope.eventId(),
                RiskAnalysisCompletedEvent.class.getSimpleName(),
                "claim-service-risk-analysis-consumer"
        );
    }

    @Test
    void fraudConsumerUpdatesClaimAndMarksProcessed() {
        UUID eventId = UUID.randomUUID();
        EventEnvelope<FraudCheckCompletedEvent> envelope = envelope(
                eventId,
                FraudCheckCompletedEvent.class.getSimpleName(),
                new FraudCheckCompletedEvent(
                        claimId,
                        companyId,
                        UUID.randomUUID(),
                        13.4,
                        "LOW",
                        List.of("clean-history"),
                        true,
                        Instant.now()
                )
        );
        Claim claim = claim(ClaimStatus.UNDER_REVIEW);
        when(processedEventService.isAlreadyProcessed(eventId, "claim-service-fraud-check-consumer")).thenReturn(false);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        new FraudCheckConsumer(processedEventService, claimRepository, historyRepository, objectMapper)
                .onMessage(envelope);

        assertEquals(13, claim.getFraudScore());
        assertEquals("LOW", claim.getFraudLevel());
        assertTrue(claim.getFraudPassed());
        verify(processedEventService).markProcessed(
                eventId,
                FraudCheckCompletedEvent.class.getSimpleName(),
                "claim-service-fraud-check-consumer"
        );
    }

    @Test
    void paymentConsumerMarksApprovedClaimPaidAndIdempotent() {
        UUID eventId = UUID.randomUUID();
        EventEnvelope<ClaimPayoutCompletedEvent> envelope = envelope(
                eventId,
                ClaimPayoutCompletedEvent.class.getSimpleName(),
                new ClaimPayoutCompletedEvent(
                        UUID.randomUUID(),
                        claimId,
                        companyId,
                        new BigDecimal("80.00"),
                        Instant.now()
                )
        );
        Claim claim = claim(ClaimStatus.APPROVED);
        when(processedEventService.isAlreadyProcessed(eventId, "claim-service-payment-consumer")).thenReturn(false);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        new PaymentConsumer(processedEventService, claimRepository, historyRepository, outboxEventService, objectMapper)
                .onMessage(envelope);

        assertEquals(ClaimStatus.PAID, claim.getStatus());
        verify(processedEventService).markProcessed(
                eventId,
                ClaimPayoutCompletedEvent.class.getSimpleName(),
                "claim-service-payment-consumer"
        );
    }

    @Test
    void duplicatePaymentEventIsSkipped() {
        UUID eventId = UUID.randomUUID();
        EventEnvelope<ClaimPayoutCompletedEvent> envelope = envelope(
                eventId,
                ClaimPayoutCompletedEvent.class.getSimpleName(),
                new ClaimPayoutCompletedEvent(UUID.randomUUID(), claimId, companyId, BigDecimal.TEN, Instant.now())
        );
        when(processedEventService.isAlreadyProcessed(eventId, "claim-service-payment-consumer")).thenReturn(true);

        new PaymentConsumer(processedEventService, claimRepository, historyRepository, outboxEventService, objectMapper)
                .onMessage(envelope);

        verify(claimRepository, never()).findById(any());
    }

    private EventEnvelope<RiskAnalysisCompletedEvent> riskEnvelope(UUID eventId) {
        return envelope(
                eventId,
                RiskAnalysisCompletedEvent.class.getSimpleName(),
                new RiskAnalysisCompletedEvent(
                        claimId,
                        companyId,
                        UUID.randomUUID(),
                        81.7,
                        "HIGH",
                        0.95,
                        List.of("high-cost"),
                        Instant.now()
                )
        );
    }

    private <T> EventEnvelope<T> envelope(UUID eventId, String eventType, T payload) {
        return new EventEnvelope<>(
                eventId,
                eventType,
                "Claim",
                claimId.toString(),
                Instant.now(),
                "corr",
                null,
                "test",
                "v1",
                payload
        );
    }

    private Claim claim(ClaimStatus status) {
        return new Claim(
                claimId,
                "CLM-" + claimId,
                UUID.randomUUID(),
                companyId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                status,
                "HOSPITAL",
                LocalDate.now(),
                new BigDecimal("100.00"),
                null,
                new BigDecimal("80.00"),
                new BigDecimal("20.00"),
                PayoutRecipientType.HOSPITAL,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );
    }
}
