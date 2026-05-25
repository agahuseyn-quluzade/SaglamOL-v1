package az.saglamol.common.kafka.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class OutboxEventService<T extends BaseOutboxEvent> {

    private final BaseOutboxEventRepository<T> repository;
    private final ObjectMapper objectMapper;
    private final Supplier<T> eventFactory;
    private final OutboxProperties properties;

    public OutboxEventService(
            BaseOutboxEventRepository<T> repository,
            ObjectMapper objectMapper,
            Supplier<T> eventFactory,
            OutboxProperties properties
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.eventFactory = eventFactory;
        this.properties = properties;
    }

    @Transactional
    public T saveEvent(String aggregateType, UUID aggregateId, String eventType, String payload) {
        T event = eventFactory.get();
        event.setAggregateType(requireText(aggregateType, "aggregateType"));
        event.setAggregateId(Objects.requireNonNull(aggregateId, "aggregateId is required"));
        event.setEventType(requireText(eventType, "eventType"));
        event.setPayload(requireText(payload, "payload"));
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(properties.getMaxRetries());
        event.setNextRetryAt(Instant.now());
        return repository.save(event);
    }

    @Transactional
    public T saveEvent(String aggregateType, UUID aggregateId, String eventType, Object payload) {
        try {
            return saveEvent(aggregateType, aggregateId, eventType, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize outbox payload", exception);
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
