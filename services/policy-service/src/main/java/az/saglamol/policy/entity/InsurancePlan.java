package az.saglamol.policy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "insurance_plan")
public class InsurancePlan {
    @Id
    private UUID id;
    private String code;
    private String name;
    private String status;
    private BigDecimal monthlyPrice;
    private Instant createdAt;

    protected InsurancePlan() {
    }
}
