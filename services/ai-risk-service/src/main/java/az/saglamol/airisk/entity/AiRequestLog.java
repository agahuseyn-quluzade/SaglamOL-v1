package az.saglamol.airisk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_request_logs")
public class AiRequestLog {
    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "request_payload", nullable = false, columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "provider_name", nullable = false, length = 80)
    private String providerName;

    @Column(nullable = false, length = 120)
    private String model;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AiRequestLog() {
    }

    public AiRequestLog(UUID id, UUID claimId, String requestPayload, String responsePayload, String providerName,
                        String model, String status, String errorMessage, long durationMs, Instant createdAt) {
        this.id = id;
        this.claimId = claimId;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.providerName = providerName;
        this.model = model;
        this.status = status;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getModel() {
        return model;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
