package az.saglamol.common.kafka.outbox;

import az.saglamol.common.events.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventEnvelopeFactory {

    private static final String DEFAULT_VERSION = "v1";

    private final ObjectMapper objectMapper;

    public EventEnvelopeFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventEnvelope<JsonNode> createEnvelope(BaseOutboxEvent outboxEvent, String serviceName) {
        try {
            return new EventEnvelope<>(
                    outboxEvent.getId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getAggregateType(),
                    outboxEvent.getAggregateId().toString(),
                    outboxEvent.getCreatedAt(),
                    outboxEvent.getId().toString(),
                    null,
                    serviceName,
                    DEFAULT_VERSION,
                    objectMapper.readTree(outboxEvent.getPayload())
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse outbox payload JSON", exception);
        }
    }
}
