package az.saglamol.claim.config;

import az.saglamol.common.events.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaProducerConfigTest {

    @Test
    void producerSerializesEventEnvelopeAsJson() {
        EventEnvelope<String> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "TestEvent",
                "Claim",
                UUID.randomUUID().toString(),
                Instant.parse("2026-05-26T10:00:00Z"),
                "correlation-id",
                null,
                "claim-service",
                "v1",
                "payload"
        );

        byte[] bytes = new JsonSerializer<>().serialize("claim.events", envelope);
        String json = new String(bytes);

        assertTrue(json.contains("\"eventType\":\"TestEvent\""));
        assertTrue(json.contains("\"producerService\":\"claim-service\""));
        assertTrue(json.contains("\"payload\":\"payload\""));
    }
}
