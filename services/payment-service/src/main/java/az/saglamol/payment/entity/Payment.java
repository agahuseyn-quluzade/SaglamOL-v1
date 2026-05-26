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
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "payment_number", nullable = false, unique = true, length = 80)
    private String paymentNumber;

    @Column(name = "policy_id")
    private UUID policyId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "patient_profile_id")
    private UUID patientProfileId;

    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "AZN";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 40)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentProvider provider = PaymentProvider.MOCK;

    @Column(name = "provider_reference", length = 160)
    private String providerReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {
    }

    public Payment(UUID id, String paymentNumber, UUID policyId, UUID insuranceCompanyId, UUID patientProfileId,
                   UUID claimId, UUID hospitalId, BigDecimal amount, String currency, PaymentType paymentType,
                   PaymentStatus status, PaymentProvider provider, String providerReference, Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.paymentNumber = paymentNumber;
        this.policyId = policyId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.patientProfileId = patientProfileId;
        this.claimId = claimId;
        this.hospitalId = hospitalId;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
        this.status = status;
        this.provider = provider;
        this.providerReference = providerReference;
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
        if (provider == null) {
            provider = PaymentProvider.MOCK;
        }
    }

    public void complete(String providerReference, Instant updatedAt) {
        this.status = PaymentStatus.COMPLETED;
        this.providerReference = providerReference;
        this.updatedAt = updatedAt;
    }

    public void fail(String providerReference, Instant updatedAt) {
        this.status = PaymentStatus.FAILED;
        this.providerReference = providerReference;
        this.updatedAt = updatedAt;
    }

    public void refund(String providerReference, Instant updatedAt) {
        this.status = PaymentStatus.REFUNDED;
        this.providerReference = providerReference;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
