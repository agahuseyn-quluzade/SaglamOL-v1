package az.saglamol.airisk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ai_risk_assessments")
public class AiRiskAssessment {
    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "doctor_profile_id")
    private UUID doctorProfileId;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private RiskLevel riskLevel;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Convert(converter = StringListJsonConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> reasons;

    @Column(name = "raw_provider_response", columnDefinition = "TEXT")
    private String rawProviderResponse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiRiskAssessmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected AiRiskAssessment() {
    }

    public AiRiskAssessment(UUID id, UUID claimId, UUID insuranceCompanyId, UUID patientProfileId, UUID policyId,
                            UUID hospitalId, UUID doctorProfileId, BigDecimal riskScore, RiskLevel riskLevel,
                            BigDecimal confidence, List<String> reasons, String rawProviderResponse,
                            AiRiskAssessmentStatus status, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.claimId = claimId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.patientProfileId = patientProfileId;
        this.policyId = policyId;
        this.hospitalId = hospitalId;
        this.doctorProfileId = doctorProfileId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.reasons = reasons;
        this.rawProviderResponse = rawProviderResponse;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public void complete(BigDecimal riskScore, RiskLevel riskLevel, BigDecimal confidence, List<String> reasons,
                         String rawProviderResponse, AiRiskAssessmentStatus status, Instant completedAt) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.reasons = reasons;
        this.rawProviderResponse = rawProviderResponse;
        this.status = status;
        this.completedAt = completedAt;
    }

    public void fail(Instant completedAt) {
        this.status = AiRiskAssessmentStatus.FAILED;
        this.completedAt = completedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public UUID getDoctorProfileId() {
        return doctorProfileId;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getRawProviderResponse() {
        return rawProviderResponse;
    }

    public AiRiskAssessmentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
