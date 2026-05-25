package az.saglamol.common.kafka.consumer;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ProcessedEventService {

    private final ProcessedEventRepository repository;

    public ProcessedEventService(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(UUID eventId, String consumerName) {
        return repository.existsByEventIdAndConsumerName(
                Objects.requireNonNull(eventId, "eventId is required"),
                requireText(consumerName, "consumerName")
        );
    }

    @Transactional
    public void markProcessed(UUID eventId, String eventType, String consumerName) {
        ProcessedEvent event = new ProcessedEvent();
        event.setEventId(Objects.requireNonNull(eventId, "eventId is required"));
        event.setEventType(requireText(eventType, "eventType"));
        event.setConsumerName(requireText(consumerName, "consumerName"));
        event.setProcessedAt(Instant.now());
        repository.save(event);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
