package az.saglamol.policy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "policies")
public class Policy {
    @Id
    private UUID id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 80)
    private String policyNumber;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "agent_profile_id")
    private UUID agentProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PolicyStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "premium_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "annual_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal annualLimit;

    @Column(name = "used_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal usedLimit;

    @Column(name = "reserved_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal reservedLimit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Policy() {
    }

    public Policy(UUID id, String policyNumber, UUID insuranceCompanyId, UUID productId, UUID patientProfileId,
                  UUID agentProfileId, PolicyStatus status, LocalDate startDate, LocalDate endDate,
                  BigDecimal premiumAmount, BigDecimal annualLimit, BigDecimal usedLimit,
                  BigDecimal reservedLimit, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.policyNumber = policyNumber;
        this.insuranceCompanyId = insuranceCompanyId;
        this.productId = productId;
        this.patientProfileId = patientProfileId;
        this.agentProfileId = agentProfileId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.premiumAmount = premiumAmount;
        this.annualLimit = annualLimit;
        this.usedLimit = usedLimit;
        this.reservedLimit = reservedLimit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void activate(Instant updatedAt) {
        this.status = PolicyStatus.ACTIVE;
        this.updatedAt = updatedAt;
    }

    public void cancel(Instant updatedAt) {
        this.status = PolicyStatus.CANCELLED;
        this.updatedAt = updatedAt;
    }

    public void suspend(Instant updatedAt) {
        this.status = PolicyStatus.SUSPENDED;
        this.updatedAt = updatedAt;
    }

    public void reserveLimit(BigDecimal amount, Instant updatedAt) {
        this.reservedLimit = this.reservedLimit.add(amount);
        this.updatedAt = updatedAt;
    }

    public void commitReservedLimit(BigDecimal amount, Instant updatedAt) {
        this.reservedLimit = this.reservedLimit.subtract(amount);
        this.usedLimit = this.usedLimit.add(amount);
        this.updatedAt = updatedAt;
    }

    public void releaseReservedLimit(BigDecimal amount, Instant updatedAt) {
        this.reservedLimit = this.reservedLimit.subtract(amount);
        this.updatedAt = updatedAt;
    }

    public BigDecimal availableLimit() {
        return annualLimit.subtract(usedLimit).subtract(reservedLimit);
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public UUID getAgentProfileId() {
        return agentProfileId;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public BigDecimal getAnnualLimit() {
        return annualLimit;
    }

    public BigDecimal getUsedLimit() {
        return usedLimit;
    }

    public BigDecimal getReservedLimit() {
        return reservedLimit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
