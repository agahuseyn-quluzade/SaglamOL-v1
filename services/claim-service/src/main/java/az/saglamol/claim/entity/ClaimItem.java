package az.saglamol.claim.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claim_item")
public class ClaimItem {
    @Id
    private UUID id;
    private UUID claimId;
    private String serviceType;
    private BigDecimal amount;
    private LocalDate serviceDate;

    protected ClaimItem() {
    }
}
