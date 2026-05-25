package az.saglamol.policy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_limit_reservations")
public class PolicyLimitReservation {
    @Id
    private UUID id;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "reserved_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal reservedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PolicyLimitReservation() {
    }

    public PolicyLimitReservation(UUID id, UUID policyId, UUID insuranceCompanyId, UUID claimId,
                                  BigDecimal reservedAmount, ReservationStatus status,
                                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.policyId = policyId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.claimId = claimId;
        this.reservedAmount = reservedAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public BigDecimal getReservedAmount() {
        return reservedAmount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
