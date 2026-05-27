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
@Table(name = "claim_decisions")
public class ClaimDecision {
    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimDecisionType decision;

    @Column(name = "decided_by", nullable = false)
    private UUID decidedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approved_amount", precision = 14, scale = 2)
    private java.math.BigDecimal approvedAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ClaimDecision() {
    }

    public ClaimDecision(UUID id, UUID claimId, ClaimDecisionType decision, UUID decidedBy,
                         String reason, java.math.BigDecimal approvedAmount, Instant createdAt) {
        this.id = id;
        this.claimId = claimId;
        this.decision = decision;
        this.decidedBy = decidedBy;
        this.reason = reason;
        this.approvedAmount = approvedAmount;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public ClaimDecisionType getDecision() {
        return decision;
    }

    public UUID getDecidedBy() {
        return decidedBy;
    }

    public String getReason() {
        return reason;
    }

    public java.math.BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
