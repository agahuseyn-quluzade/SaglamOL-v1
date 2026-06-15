package az.saglamol.policy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_limit_reservation")
public class PolicyLimitReservation {
    @Id
    private UUID id;
    private UUID policyId;
    private UUID claimId;
    private String serviceType;
    private BigDecimal reservedAmount;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    protected PolicyLimitReservation() {
    }
}
