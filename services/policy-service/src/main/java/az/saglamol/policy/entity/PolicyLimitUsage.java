package az.saglamol.policy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "policy_limit_usage")
public class PolicyLimitUsage {
    @Id
    private UUID id;
    private UUID policyId;
    private String serviceType;
    private BigDecimal annualLimit;
    private BigDecimal usedAmount;
    private BigDecimal reservedAmount;
    @Version
    private Long version;

    protected PolicyLimitUsage() {
    }
}
