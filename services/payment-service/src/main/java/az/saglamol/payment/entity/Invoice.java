package az.saglamol.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 80)
    private String invoiceNumber;

    @Column(name = "policy_id")
    private UUID policyId;

    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "patient_profile_id")
    private UUID patientProfileId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "AZN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InvoiceStatus status;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Invoice() {
    }

    public Invoice(UUID id, String invoiceNumber, UUID policyId, UUID claimId, UUID insuranceCompanyId, UUID hospitalId,
                   UUID patientProfileId, BigDecimal amount, String currency, InvoiceStatus status, Instant issuedAt,
                   Instant paidAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.policyId = policyId;
        this.claimId = claimId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.hospitalId = hospitalId;
        this.patientProfileId = patientProfileId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.issuedAt = issuedAt;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (currency == null || currency.isBlank()) {
            currency = "AZN";
        }
    }

    public void issue(Instant now) {
        this.status = InvoiceStatus.ISSUED;
        this.issuedAt = now;
        this.updatedAt = now;
    }

    public void markPaid(Instant now) {
        this.status = InvoiceStatus.PAID;
        this.paidAt = now;
        this.updatedAt = now;
    }

    public void cancel(Instant now) {
        this.status = InvoiceStatus.CANCELLED;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
