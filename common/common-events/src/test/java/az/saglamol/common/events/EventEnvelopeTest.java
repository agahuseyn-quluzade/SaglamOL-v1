package az.saglamol.common.events;

import az.saglamol.common.events.policy.PolicyCreatedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventEnvelopeTest {

    @Test
    void serializesGenericEventEnvelope() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        PolicyCreatedEvent payload = new PolicyCreatedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "POL-1001",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                new BigDecimal("120.50"),
                Instant.parse("2026-05-25T10:00:00Z")
        );

        EventEnvelope<PolicyCreatedEvent> envelope = new EventEnvelope<>(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "PolicyCreatedEvent",
                "POLICY",
                "11111111-1111-1111-1111-111111111111",
                Instant.parse("2026-05-25T10:00:01Z"),
                "corr-1",
                null,
                "policy-service",
                "v1",
                payload
        );

        String json = objectMapper.writeValueAsString(envelope);
        JsonNode root = objectMapper.readTree(json);

        assertEquals("PolicyCreatedEvent", root.get("eventType").asText());
        assertEquals("POLICY", root.get("aggregateType").asText());
        assertEquals("policy-service", root.get("producerService").asText());
        assertEquals("POL-1001", root.get("payload").get("policyNumber").asText());
        assertTrue(root.has("correlationId"));
        assertFalse(root.has("causationId"));
    }
}
