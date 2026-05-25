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
@Table(name = "coverage_rules")
public class CoverageRule {
    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 80)
    private ServiceType serviceType;

    @Column(name = "coverage_percent", nullable = false)
    private Integer coveragePercent;

    @Column(name = "max_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "waiting_period_days", nullable = false)
    private Integer waitingPeriodDays;

    @Column(name = "requires_pre_approval", nullable = false)
    private boolean requiresPreApproval;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RuleStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CoverageRule() {
    }

    public CoverageRule(UUID id, UUID productId, ServiceType serviceType, Integer coveragePercent,
                        BigDecimal maxAmount, Integer waitingPeriodDays, boolean requiresPreApproval,
                        RuleStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.productId = productId;
        this.serviceType = serviceType;
        this.coveragePercent = coveragePercent;
        this.maxAmount = maxAmount;
        this.waitingPeriodDays = waitingPeriodDays;
        this.requiresPreApproval = requiresPreApproval;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void update(Integer coveragePercent, BigDecimal maxAmount, Integer waitingPeriodDays,
                       boolean requiresPreApproval, Instant updatedAt) {
        this.coveragePercent = coveragePercent;
        this.maxAmount = maxAmount;
        this.waitingPeriodDays = waitingPeriodDays;
        this.requiresPreApproval = requiresPreApproval;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(RuleStatus status, Instant updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public UUID getProductId() {
        return productId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public Integer getCoveragePercent() {
        return coveragePercent;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public Integer getWaitingPeriodDays() {
        return waitingPeriodDays;
    }

    public boolean isRequiresPreApproval() {
        return requiresPreApproval;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
