package az.saglamol.common.kafka.consumer;

import az.saglamol.common.events.EventEnvelope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotentEventConsumerTest {

    @Test
    void skipsDuplicateEvent() {
        ProcessedEventService processedEventService = mock(ProcessedEventService.class);
        EventEnvelope<String> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "payment-consumer")).thenReturn(true);
        CountingConsumer consumer = new CountingConsumer(processedEventService, "payment-consumer");

        consumer.consume(envelope);

        assertEquals(0, consumer.handledCount());
        verify(processedEventService, never()).markProcessed(envelope.eventId(), envelope.eventType(), "payment-consumer");
    }

    @Test
    void handlesAndMarksNewEvent() {
        ProcessedEventService processedEventService = mock(ProcessedEventService.class);
        EventEnvelope<String> envelope = envelope();
        when(processedEventService.isAlreadyProcessed(envelope.eventId(), "payment-consumer")).thenReturn(false);
        CountingConsumer consumer = new CountingConsumer(processedEventService, "payment-consumer");

        consumer.consume(envelope);

        assertEquals(1, consumer.handledCount());
        verify(processedEventService).markProcessed(envelope.eventId(), envelope.eventType(), "payment-consumer");
    }

    private EventEnvelope<String> envelope() {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                "PaymentCompletedEvent",
                "PAYMENT",
                UUID.randomUUID().toString(),
                Instant.parse("2026-05-25T08:00:00Z"),
                "corr-1",
                null,
                "payment-service",
                "v1",
                "{}"
        );
    }

    private static class CountingConsumer extends IdempotentEventConsumer {
        private final AtomicInteger handled = new AtomicInteger();

        CountingConsumer(ProcessedEventService processedEventService, String consumerName) {
            super(processedEventService, consumerName);
        }

        @Override
        protected void handleEvent(EventEnvelope<?> envelope) {
            handled.incrementAndGet();
        }

        int handledCount() {
            return handled.get();
        }
    }
}
