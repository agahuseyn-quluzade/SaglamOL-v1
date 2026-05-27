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
@Table(name = "fraud_assessments")
public class FraudAssessment {
    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "doctor_profile_id")
    private UUID doctorProfileId;

    @Column(name = "fraud_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal fraudScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_level", nullable = false, length = 30)
    private FraudLevel fraudLevel;

    @Column(name = "manual_review_required", nullable = false)
    private boolean manualReviewRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FraudAssessmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected FraudAssessment() {
    }

    public FraudAssessment(UUID id, UUID claimId, UUID insuranceCompanyId, UUID patientProfileId,
                           UUID hospitalId, UUID doctorProfileId, BigDecimal fraudScore, FraudLevel fraudLevel,
                           boolean manualReviewRequired, FraudAssessmentStatus status,
                           Instant createdAt, Instant completedAt) {
        this.id = id;
        this.claimId = claimId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.patientProfileId = patientProfileId;
        this.hospitalId = hospitalId;
        this.doctorProfileId = doctorProfileId;
        this.fraudScore = fraudScore;
        this.fraudLevel = fraudLevel;
        this.manualReviewRequired = manualReviewRequired;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public void complete(BigDecimal fraudScore, FraudLevel fraudLevel, boolean manualReviewRequired, Instant completedAt) {
        this.fraudScore = fraudScore;
        this.fraudLevel = fraudLevel;
        this.manualReviewRequired = manualReviewRequired;
        this.status = FraudAssessmentStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void fail(Instant completedAt) {
        this.status = FraudAssessmentStatus.FAILED;
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

    public UUID getHospitalId() {
        return hospitalId;
    }

    public UUID getDoctorProfileId() {
        return doctorProfileId;
    }

    public BigDecimal getFraudScore() {
        return fraudScore;
    }

    public FraudLevel getFraudLevel() {
        return fraudLevel;
    }

    public boolean isManualReviewRequired() {
        return manualReviewRequired;
    }

    public FraudAssessmentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
