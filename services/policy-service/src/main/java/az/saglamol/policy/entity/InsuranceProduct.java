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
@Table(name = "insurance_products")
public class InsuranceProduct {
    @Id
    private UUID id;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "product_code", nullable = false, length = 80)
    private String productCode;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_type", nullable = false, length = 40)
    private CoverageType coverageType;

    @Column(name = "premium_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "annual_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal annualLimit;

    @Column(nullable = false, length = 3)
    private String currency = "AZN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InsuranceProductStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InsuranceProduct() {
    }

    public InsuranceProduct(UUID id, UUID insuranceCompanyId, String productCode, String name, String description,
                            CoverageType coverageType, BigDecimal premiumAmount, BigDecimal annualLimit,
                            String currency, InsuranceProductStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.insuranceCompanyId = insuranceCompanyId;
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.coverageType = coverageType;
        this.premiumAmount = premiumAmount;
        this.annualLimit = annualLimit;
        this.currency = currency == null || currency.isBlank() ? "AZN" : currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void update(String name, String description, CoverageType coverageType, BigDecimal premiumAmount,
                       BigDecimal annualLimit, String currency, Instant updatedAt) {
        this.name = name;
        this.description = description;
        this.coverageType = coverageType;
        this.premiumAmount = premiumAmount;
        this.annualLimit = annualLimit;
        this.currency = currency == null || currency.isBlank() ? "AZN" : currency;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(InsuranceProductStatus status, Instant updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CoverageType getCoverageType() {
        return coverageType;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public BigDecimal getAnnualLimit() {
        return annualLimit;
    }

    public String getCurrency() {
        return currency;
    }

    public InsuranceProductStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
