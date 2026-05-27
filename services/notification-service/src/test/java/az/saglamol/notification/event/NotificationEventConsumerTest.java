package az.saglamol.notification.event;

import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.claim.ClaimSubmittedEvent;
import az.saglamol.common.events.payment.PaymentCompletedEvent;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.notification.dto.SendNotificationRequest;
import az.saglamol.notification.service.NotificationService;
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

class NotificationEventConsumerTest {

    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final NotificationEventConsumer consumer = new NotificationEventConsumer(
            processedEventService,
            notificationService,
            JsonMapper.builder().findAndAddModules().build()
    );

    @Test
    void duplicateEventIsSkipped() {
        EventEnvelope<ClaimSubmittedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "notification-service-event-consumer"))
                .thenReturn(true);

        consumer.onMessage(envelope);

        verify(notificationService, never()).sendFromEvent(any());
    }

    @Test
    void claimSubmittedEventCreatesNotification() {
        EventEnvelope<ClaimSubmittedEvent> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "notification-service-event-consumer"))
                .thenReturn(false);

        consumer.onMessage(envelope);

        verify(notificationService).sendFromEvent(any(SendNotificationRequest.class));
        verify(processedEventService).markProcessed(envelope.eventId(), "ClaimSubmittedEvent",
                "notification-service-event-consumer");
    }

    @Test
    void paymentCompletedEventCreatesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        EventEnvelope<PaymentCompletedEvent> envelope = new EventEnvelope<>(
                eventId,
                PaymentCompletedEvent.class.getSimpleName(),
                "Payment",
                paymentId.toString(),
                Instant.now(),
                "corr",
                null,
                "payment-service",
                "1",
                new PaymentCompletedEvent(
                        paymentId,
                        "PAY-1",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50.00"),
                        "AZN",
                        "POLICY_PREMIUM",
                        Instant.now()
                )
        );
        when(processedEventService.isAlreadyProcessed(eventId, "notification-service-event-consumer"))
                .thenReturn(false);

        consumer.onMessage(envelope);

        verify(notificationService).sendFromEvent(any(SendNotificationRequest.class));
        verify(processedEventService).markProcessed(eventId, PaymentCompletedEvent.class.getSimpleName(),
                "notification-service-event-consumer");
    }

    private EventEnvelope<ClaimSubmittedEvent> envelope() {
        UUID eventId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        ClaimSubmittedEvent payload = new ClaimSubmittedEvent(claimId, "CLM-1", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "CONSULTATION",
                new BigDecimal("125.00"), List.of(UUID.randomUUID()), Instant.now());
        return new EventEnvelope<>(eventId, ClaimSubmittedEvent.class.getSimpleName(), "Claim", claimId.toString(),
                Instant.now(), "corr", null, "claim-service", "1", payload);
    }
}
