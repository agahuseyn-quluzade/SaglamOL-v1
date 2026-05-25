package az.saglamol.common.kafka.outbox;

import az.saglamol.common.events.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventEnvelopeFactoryTest {

    @Test
    void createsValidEnvelope() {
        UUID outboxId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        TestOutboxEvent event = new TestOutboxEvent();
        event.setId(outboxId);
        event.setAggregateType("CLAIM");
        event.setAggregateId(aggregateId);
        event.setEventType("ClaimSubmittedEvent");
        event.setPayload("{\"claimNumber\":\"CLM-1\"}");
        event.setCreatedAt(Instant.parse("2026-05-25T08:00:00Z"));

        EventEnvelope<JsonNode> envelope = new EventEnvelopeFactory(new ObjectMapper())
                .createEnvelope(event, "claim-service");

        assertEquals(outboxId, envelope.eventId());
        assertEquals("ClaimSubmittedEvent", envelope.eventType());
        assertEquals("CLAIM", envelope.aggregateType());
        assertEquals(aggregateId.toString(), envelope.aggregateId());
        assertEquals("claim-service", envelope.producerService());
        assertEquals("v1", envelope.version());
        assertEquals("CLM-1", envelope.payload().get("claimNumber").asText());
    }
}
