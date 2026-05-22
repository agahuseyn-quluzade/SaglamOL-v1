package az.saglamol.airisk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_analysis")
public class RiskAnalysis {
    @Id
    private UUID id;
    private UUID claimId;
    private Integer riskScore;
    private String riskLevel;
    private BigDecimal confidence;
    private String modelVersion;
    private Instant createdAt;

    protected RiskAnalysis() {
    }
}
