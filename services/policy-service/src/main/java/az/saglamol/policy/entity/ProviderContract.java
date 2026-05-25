package az.saglamol.policy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "provider_contracts")
public class ProviderContract {
    @Id
    private UUID id;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "contract_number", nullable = false, length = 80)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProviderContractStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_model", nullable = false, length = 40)
    private PayoutModel payoutModel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProviderContract() {
    }

    public ProviderContract(UUID id, UUID insuranceCompanyId, UUID hospitalId, UUID productId,
                            String contractNumber, LocalDate startDate, LocalDate endDate,
                            ProviderContractStatus status, PayoutModel payoutModel,
                            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.insuranceCompanyId = insuranceCompanyId;
        this.hospitalId = hospitalId;
        this.productId = productId;
        this.contractNumber = contractNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.payoutModel = payoutModel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void terminate(Instant updatedAt) {
        this.status = ProviderContractStatus.TERMINATED;
        this.updatedAt = updatedAt;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ProviderContractStatus getStatus() {
        return status;
    }

    public PayoutModel getPayoutModel() {
        return payoutModel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
