package az.saglamol.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_event_log")
public class PaymentEventLog {
    @Id
    private UUID id;
    private UUID paymentTransactionId;
    private String eventType;
    private String payload;
    private Instant createdAt;

    protected PaymentEventLog() {
    }
}
