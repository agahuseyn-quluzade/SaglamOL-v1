package az.saglamol.policy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "coverage_rule")
public class CoverageRule {
    @Id
    private UUID id;
    private UUID planId;
    private String serviceType;
    private Integer coveragePercent;
    private BigDecimal annualLimit;
    private Integer waitingPeriodDays;

    protected CoverageRule() {
    }
}
