package az.saglamol.fraud.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "fraud_signal")
public class FraudSignal {
    @Id
    private UUID id;
    private UUID fraudCheckId;
    private String signalType;
    private String severity;
    private String description;

    protected FraudSignal() {
    }
}
