package az.saglamol.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {
    @Id
    private UUID id;
    private UUID policyId;
    private UUID claimId;
    private UUID payerUserId;
    private BigDecimal amount;
    private String currency;
    private String type;
    private String status;
    private String provider;
    private String providerReference;
    private Instant createdAt;
    private Instant updatedAt;

    protected PaymentTransaction() {
    }
}
