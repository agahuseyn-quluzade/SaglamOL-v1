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
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TransactionStatus status;

    @Column(name = "provider_response", length = 2000)
    private String providerResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(UUID id, UUID paymentId, TransactionType transactionType, BigDecimal amount,
                              TransactionStatus status, String providerResponse, Instant createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.providerResponse = providerResponse;
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getProviderResponse() {
        return providerResponse;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
