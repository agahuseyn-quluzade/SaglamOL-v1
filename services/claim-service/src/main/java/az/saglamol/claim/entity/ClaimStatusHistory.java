package az.saglamol.claim.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_status_history")
public class ClaimStatusHistory {
    @Id
    private UUID id;
    private UUID claimId;
    private String oldStatus;
    private String newStatus;
    private Instant changedAt;

    protected ClaimStatusHistory() {
    }
}
