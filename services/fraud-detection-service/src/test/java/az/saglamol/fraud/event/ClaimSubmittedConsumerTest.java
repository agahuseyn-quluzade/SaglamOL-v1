package az.saglamol.fraud.event;

import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.claim.ClaimSubmittedEvent;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.fraud.client.ClaimDetailResponse;
import az.saglamol.fraud.service.FraudAssessmentService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaimSubmittedConsumerTest {

    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);
    private final FraudAssessmentService assessmentService = mock(FraudAssessmentService.class);
    private final ClaimSubmittedConsumer consumer = new ClaimSubmittedConsumer(
            processedEventService,
            assessmentService,
            JsonMapper.builder().findAndAddModules().build()
    );

    @Test
    void duplicateEventIsSkipped() {
        EventEnvelope<ClaimSubmittedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "fraud-detection-claim-submitted-consumer"))
                .thenReturn(true);

        consumer.onMessage(envelope);

        verify(assessmentService, never()).checkSubmittedClaim(any());
        verify(processedEventService, never()).markProcessed(any(), any(), any());
    }

    @Test
    void newEventRunsFraudAssessmentAndMarksProcessed() {
        EventEnvelope<ClaimSubmittedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "fraud-detection-claim-submitted-consumer"))
                .thenReturn(false);

        consumer.onMessage(envelope);

        var captor = forClass(ClaimDetailResponse.class);
        verify(assessmentService).checkSubmittedClaim(captor.capture());
        assertEquals(envelope.payload().documentIds(), captor.getValue().documentIds());
        verify(processedEventService).markProcessed(envelope.eventId(), "ClaimSubmittedEvent",
                "fraud-detection-claim-submitted-consumer");
    }

    private EventEnvelope<ClaimSubmittedEvent> envelope() {
        UUID eventId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        ClaimSubmittedEvent payload = new ClaimSubmittedEvent(
                claimId,
                "CLM-1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CONSULTATION",
                new BigDecimal("125.00"),
                List.of(documentId),
                Instant.now()
        );
        return new EventEnvelope<>(
                eventId,
                ClaimSubmittedEvent.class.getSimpleName(),
                "Claim",
                claimId.toString(),
                Instant.now(),
                "corr",
                null,
                "claim-service",
                "1",
                payload
        );
    }
}
