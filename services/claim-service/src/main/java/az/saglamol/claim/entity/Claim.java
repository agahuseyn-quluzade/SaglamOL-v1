package az.saglamol.claim.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim")
public class Claim {
    @Id
    private UUID id;
    private UUID patientId;
    private UUID policyId;
    private String status;
    private String claimType;
    private BigDecimal totalAmount;
    private Instant createdAt;

    protected Claim() {
    }
}
