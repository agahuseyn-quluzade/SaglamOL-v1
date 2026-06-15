package az.saglamol.claim.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_decision")
public class ClaimDecision {
    @Id
    private UUID id;
    private UUID claimId;
    private String decision;
    private UUID decidedBy;
    private String reason;
    private Instant createdAt;

    protected ClaimDecision() {
    }
}
