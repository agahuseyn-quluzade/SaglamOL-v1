package az.saglamol.airisk.event;

import az.saglamol.airisk.client.ClaimDetailResponse;
import az.saglamol.airisk.service.AiRiskAssessmentService;
import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.claim.ClaimSubmittedEvent;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaimSubmittedConsumerTest {

    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);
    private final AiRiskAssessmentService assessmentService = mock(AiRiskAssessmentService.class);
    private final ClaimSubmittedConsumer consumer = new ClaimSubmittedConsumer(
            processedEventService,
            assessmentService,
            JsonMapper.builder().findAndAddModules().build()
    );

    @Test
    void duplicateEventIsSkipped() {
        EventEnvelope<ClaimSubmittedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "ai-risk-claim-submitted-consumer"))
                .thenReturn(true);

        consumer.onMessage(envelope);

        verify(assessmentService, never()).assessSubmittedClaim(any());
        verify(processedEventService, never()).markProcessed(any(), any(), any());
    }

    @Test
    void newEventRunsAssessmentAndMarksProcessed() {
        EventEnvelope<ClaimSubmittedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "ai-risk-claim-submitted-consumer"))
                .thenReturn(false);

        consumer.onMessage(envelope);

        verify(assessmentService).assessSubmittedClaim(any(ClaimDetailResponse.class));
        verify(processedEventService).markProcessed(envelope.eventId(), "ClaimSubmittedEvent",
                "ai-risk-claim-submitted-consumer");
    }

    private EventEnvelope<ClaimSubmittedEvent> envelope() {
        UUID eventId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
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
                List.of(UUID.randomUUID()),
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
