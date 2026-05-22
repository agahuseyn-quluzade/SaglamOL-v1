package az.saglamol.fraud.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_check")
public class FraudCheck {
    @Id
    private UUID id;
    private UUID claimId;
    private Integer fraudScore;
    private String status;
    private Instant checkedAt;

    protected FraudCheck() {
    }
}
