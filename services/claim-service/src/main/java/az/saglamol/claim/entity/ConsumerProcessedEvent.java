package az.saglamol.claim.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@IdClass(ConsumerProcessedEvent.Key.class)
@Table(name = "consumer_processed_event")
public class ConsumerProcessedEvent {
    @Id
    private UUID eventId;
    @Id
    private String consumerName;
    private Instant processedAt;

    protected ConsumerProcessedEvent() {
    }

    public static class Key implements Serializable {
        private UUID eventId;
        private String consumerName;
    }
}
