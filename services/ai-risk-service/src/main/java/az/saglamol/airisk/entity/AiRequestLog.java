package az.saglamol.airisk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_request_log")
public class AiRequestLog {
    @Id
    private UUID id;
    private UUID claimId;
    private String provider;
    private Long latencyMs;
    private String status;
    private Instant createdAt;

    protected AiRequestLog() {
    }
}
