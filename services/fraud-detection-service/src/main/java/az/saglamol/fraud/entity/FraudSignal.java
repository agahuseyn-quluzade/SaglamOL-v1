package az.saglamol.fraud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_signals")
public class FraudSignal {
    @Id
    private UUID id;

    @Column(name = "fraud_assessment_id", nullable = false)
    private UUID fraudAssessmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false, length = 60)
    private FraudSignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FraudSignalSeverity severity;

    @Column(name = "score_impact", nullable = false, precision = 5, scale = 4)
    private BigDecimal scoreImpact;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FraudSignal() {
    }

    public FraudSignal(UUID id, UUID fraudAssessmentId, FraudSignalType signalType,
                       FraudSignalSeverity severity, BigDecimal scoreImpact, String message, Instant createdAt) {
        this.id = id;
        this.fraudAssessmentId = fraudAssessmentId;
        this.signalType = signalType;
        this.severity = severity;
        this.scoreImpact = scoreImpact;
        this.message = message;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFraudAssessmentId() {
        return fraudAssessmentId;
    }

    public FraudSignalType getSignalType() {
        return signalType;
    }

    public FraudSignalSeverity getSeverity() {
        return severity;
    }

    public BigDecimal getScoreImpact() {
        return scoreImpact;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
