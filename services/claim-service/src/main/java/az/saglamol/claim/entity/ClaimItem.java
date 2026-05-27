package az.saglamol.claim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claim_items")
public class ClaimItem {
    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "service_code", length = 80)
    private String serviceCode;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "created_at", nullable = false)
    private java.time.Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.Instant updatedAt;

    protected ClaimItem() {
    }

    public ClaimItem(UUID id, UUID claimId, String description, String serviceCode, BigDecimal amount,
                     Integer quantity, LocalDate serviceDate, UUID documentId,
                     java.time.Instant createdAt, java.time.Instant updatedAt) {
        this.id = id;
        this.claimId = claimId;
        this.description = description;
        this.serviceCode = serviceCode;
        this.amount = amount;
        this.quantity = quantity;
        this.serviceDate = serviceDate;
        this.documentId = documentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public String getDescription() {
        return description;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }

    public java.time.Instant getUpdatedAt() {
        return updatedAt;
    }
}
