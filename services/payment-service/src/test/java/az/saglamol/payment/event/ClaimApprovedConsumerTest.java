package az.saglamol.payment.event;

import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.claim.ClaimApprovedEvent;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.payment.service.PaymentService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaimApprovedConsumerTest {

    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);
    private final PaymentService paymentService = mock(PaymentService.class);
    private final ClaimApprovedConsumer consumer = new ClaimApprovedConsumer(
            processedEventService,
            paymentService,
            JsonMapper.builder().findAndAddModules().build()
    );

    @Test
    void duplicateClaimApprovedEventIsSkipped() {
        EventEnvelope<ClaimApprovedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "payment-service-claim-approved-consumer"))
                .thenReturn(true);

        consumer.onMessage(envelope);

        verify(paymentService, never()).createClaimPayoutFromApprovedClaim(any());
        verify(processedEventService, never()).markProcessed(any(), any(), any());
    }

    @Test
    void claimApprovedEventCreatesPayoutAndMarksProcessed() {
        EventEnvelope<ClaimApprovedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "payment-service-claim-approved-consumer"))
                .thenReturn(false);

        consumer.onMessage(envelope);

        verify(paymentService).createClaimPayoutFromApprovedClaim(any(ClaimApprovedEvent.class));
        verify(processedEventService).markProcessed(envelope.eventId(), ClaimApprovedEvent.class.getSimpleName(),
                "payment-service-claim-approved-consumer");
    }

    private EventEnvelope<ClaimApprovedEvent> envelope() {
        UUID eventId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        return new EventEnvelope<>(
                eventId,
                ClaimApprovedEvent.class.getSimpleName(),
                "Claim",
                claimId.toString(),
                Instant.now(),
                "corr",
                null,
                "claim-service",
                "1",
                new ClaimApprovedEvent(
                        claimId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("100.00"),
                        UUID.randomUUID().toString(),
                        Instant.now()
                )
        );
    }
}
