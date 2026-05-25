package az.saglamol.common.kafka.outbox;

import az.saglamol.common.events.EventEnvelope;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, EventEnvelope<?> envelope) {
        try {
            kafkaTemplate.send(topic, key, envelope).get();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to publish Kafka event", exception);
        }
    }
}
