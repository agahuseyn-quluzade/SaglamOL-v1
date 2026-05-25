package az.saglamol.common.kafka.outbox;

import az.saglamol.common.events.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class OutboxPublisherScheduler<T extends BaseOutboxEvent> {

    private final BaseOutboxEventRepository<T> repository;
    private final EventEnvelopeFactory envelopeFactory;
    private final KafkaPublisher kafkaPublisher;
    private final OutboxProperties properties;
    private final String serviceName;

    public OutboxPublisherScheduler(
            BaseOutboxEventRepository<T> repository,
            EventEnvelopeFactory envelopeFactory,
            KafkaPublisher kafkaPublisher,
            OutboxProperties properties,
            String serviceName
    ) {
        this.repository = repository;
        this.envelopeFactory = envelopeFactory;
        this.kafkaPublisher = kafkaPublisher;
        this.properties = properties;
        this.serviceName = serviceName;
    }

    @Scheduled(fixedDelayString = "${outbox.scheduler.interval:5000}")
    @Transactional
    public void publishPendingEvents() {
        if (!properties.getScheduler().isEnabled()) {
            return;
        }
        List<T> events = repository.findTop100ByStatusAndNextRetryAtLessThanEqualAndRetryCountLessThanOrderByCreatedAtAsc(
                OutboxEventStatus.PENDING,
                Instant.now(),
                properties.getMaxRetries()
        );
        events.forEach(this::publishOne);
    }

    private void publishOne(T event) {
        try {
            EventEnvelope<JsonNode> envelope = envelopeFactory.createEnvelope(event, serviceName);
            kafkaPublisher.publish(requireTopic(), event.getAggregateId().toString(), envelope);
            event.markPublished(Instant.now());
        } catch (Exception exception) {
            int nextRetryCount = event.getRetryCount() + 1;
            Instant nextRetryAt = Instant.now().plus(Duration.ofSeconds(30L * nextRetryCount));
            event.markPublishFailure(rootMessage(exception), nextRetryAt);
        }
        repository.save(event);
    }

    private String requireTopic() {
        String topic = properties.getTopic();
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("outbox.topic must be configured");
        }
        return topic;
    }

    private String rootMessage(Exception exception) {
        Throwable cursor = exception;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        return cursor.getMessage() == null ? cursor.getClass().getSimpleName() : cursor.getMessage();
    }
}
