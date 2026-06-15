package az.saglamol.common.events;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventEnvelopeTest {

    @Test
    void createsEventEnvelope() {
        UUID eventId = UUID.randomUUID();

        EventEnvelope<Map<String, String>> envelope = new EventEnvelope<>(
                eventId,
                "TestEvent",
                "1.0.0",
                "TEST",
                "aggregate-1",
                Instant.parse("2026-05-20T00:00:00Z"),
                "test-service",
                "correlation-1",
                Map.of("key", "value")
        );

        assertEquals(eventId, envelope.eventId());
        assertEquals("TestEvent", envelope.eventType());
        assertEquals("value", envelope.payload().get("key"));
    }
}
