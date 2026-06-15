package az.saglamol.claim.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id
    private UUID id;
    private String aggregateType;
    private UUID aggregateId;
    private String eventType;
    private String schemaVersion;
    private String payload;
    private String status;
    private Integer retryCount;
    private Instant nextRetryAt;
    private String lastError;
    private Instant createdAt;
    private Instant publishedAt;

    protected OutboxEvent() {
    }
}
