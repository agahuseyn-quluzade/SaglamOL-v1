package az.saglamol.claim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_document_references")
public class ClaimDocumentReference {
    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "document_type", nullable = false, length = 80)
    private String documentType;

    @Column(nullable = false)
    private boolean required;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimDocumentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ClaimDocumentReference() {
    }

    public ClaimDocumentReference(UUID id, UUID claimId, UUID documentId, String documentType,
                                  boolean required, ClaimDocumentStatus status, Instant createdAt) {
        this.id = id;
        this.claimId = claimId;
        this.documentId = documentId;
        this.documentType = documentType;
        this.required = required;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public boolean isRequired() {
        return required;
    }

    public ClaimDocumentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
