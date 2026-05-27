package az.saglamol.healthrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "health_access_log")
public class HealthAccessLog {
    @Id
    private UUID id;

    @Column(name = "health_record_id")
    private UUID healthRecordId;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "accessed_by_user_id", nullable = false)
    private UUID accessedByUserId;

    @Column(name = "access_role", nullable = false, length = 80)
    private String accessRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    protected HealthAccessLog() {
    }

    public HealthAccessLog(UUID id, UUID healthRecordId, UUID documentId, UUID accessedByUserId,
                           String accessRole, String reason, Instant accessedAt) {
        this.id = id;
        this.healthRecordId = healthRecordId;
        this.documentId = documentId;
        this.accessedByUserId = accessedByUserId;
        this.accessRole = accessRole;
        this.reason = reason;
        this.accessedAt = accessedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHealthRecordId() {
        return healthRecordId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getAccessedByUserId() {
        return accessedByUserId;
    }

    public String getAccessRole() {
        return accessRole;
    }

    public String getReason() {
        return reason;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }
}
