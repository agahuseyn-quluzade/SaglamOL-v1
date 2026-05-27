package az.saglamol.claim.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims")
public class Claim {
    @Id
    private UUID id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 80)
    private String claimNumber;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "doctor_profile_id")
    private UUID doctorProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ClaimStatus status;

    @Column(name = "service_type", nullable = false, length = 80)
    private String serviceType;

    @Column(name = "treatment_date", nullable = false)
    private LocalDate treatmentDate;

    @Column(name = "claim_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "approved_amount", precision = 14, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "covered_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal coveredAmount;

    @Column(name = "patient_pay_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal patientPayAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_recipient_type", nullable = false, length = 30)
    private PayoutRecipientType payoutRecipientType;

    @Column(name = "policy_reservation_id")
    private UUID policyReservationId;

    @Column(name = "diagnosis_code", length = 80)
    private String diagnosisCode;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_level", length = 30)
    private String riskLevel;

    @Column(name = "fraud_score")
    private Integer fraudScore;

    @Column(name = "fraud_level", length = 30)
    private String fraudLevel;

    @Column(name = "fraud_passed")
    private Boolean fraudPassed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Claim() {
    }

    public Claim(UUID id, String claimNumber, UUID policyId, UUID insuranceCompanyId, UUID patientProfileId,
                 UUID hospitalId, UUID doctorProfileId, ClaimStatus status, String serviceType,
                 LocalDate treatmentDate, BigDecimal claimAmount, BigDecimal approvedAmount,
                 BigDecimal coveredAmount, BigDecimal patientPayAmount, PayoutRecipientType payoutRecipientType,
                 UUID policyReservationId, String diagnosisCode, String reason, String notes,
                 Integer riskScore, String riskLevel, Integer fraudScore, String fraudLevel,
                 Boolean fraudPassed, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.claimNumber = claimNumber;
        this.policyId = policyId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.patientProfileId = patientProfileId;
        this.hospitalId = hospitalId;
        this.doctorProfileId = doctorProfileId;
        this.status = status;
        this.serviceType = serviceType;
        this.treatmentDate = treatmentDate;
        this.claimAmount = claimAmount;
        this.approvedAmount = approvedAmount;
        this.coveredAmount = coveredAmount;
        this.patientPayAmount = patientPayAmount;
        this.payoutRecipientType = payoutRecipientType;
        this.policyReservationId = policyReservationId;
        this.diagnosisCode = diagnosisCode;
        this.reason = reason;
        this.notes = notes;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.fraudScore = fraudScore;
        this.fraudLevel = fraudLevel;
        this.fraudPassed = fraudPassed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void submit(BigDecimal claimAmount, BigDecimal coveredAmount, BigDecimal patientPayAmount,
                       PayoutRecipientType payoutRecipientType, UUID policyReservationId, Instant updatedAt) {
        this.claimAmount = claimAmount;
        this.coveredAmount = coveredAmount;
        this.patientPayAmount = patientPayAmount;
        this.payoutRecipientType = payoutRecipientType;
        this.policyReservationId = policyReservationId;
        changeStatus(ClaimStatus.SUBMITTED, updatedAt);
    }

    public void startReview(Instant updatedAt) {
        changeStatus(ClaimStatus.UNDER_REVIEW, updatedAt);
    }

    public void requestMoreDocuments(String reason, Instant updatedAt) {
        this.reason = reason;
        changeStatus(ClaimStatus.NEEDS_MORE_DOCUMENTS, updatedAt);
    }

    public void approve(BigDecimal approvedAmount, String reason, Instant updatedAt) {
        this.approvedAmount = approvedAmount;
        this.reason = reason;
        changeStatus(ClaimStatus.APPROVED, updatedAt);
    }

    public void reject(String reason, Instant updatedAt) {
        this.reason = reason;
        changeStatus(ClaimStatus.REJECTED, updatedAt);
    }

    public void cancel(String reason, Instant updatedAt) {
        this.reason = reason;
        changeStatus(ClaimStatus.CANCELLED, updatedAt);
    }

    public void updateRisk(Integer riskScore, String riskLevel, Instant updatedAt) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.updatedAt = updatedAt;
    }

    public void updateFraud(Integer fraudScore, String fraudLevel, boolean fraudPassed, Instant updatedAt) {
        this.fraudScore = fraudScore;
        this.fraudLevel = fraudLevel;
        this.fraudPassed = fraudPassed;
        this.updatedAt = updatedAt;
    }

    public void markPaid(Instant updatedAt) {
        changeStatus(ClaimStatus.PAID, updatedAt);
    }

    public void markPayoutFailed(String reason, Instant updatedAt) {
        this.reason = reason;
        changeStatus(ClaimStatus.PAYOUT_FAILED, updatedAt);
    }

    private void changeStatus(ClaimStatus status, Instant updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public UUID getPolicyId() {
        return policyId;
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

    public ClaimStatus getStatus() {
        return status;
    }

    public String getServiceType() {
        return serviceType;
    }

    public LocalDate getTreatmentDate() {
        return treatmentDate;
    }

    public BigDecimal getClaimAmount() {
        return claimAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public BigDecimal getCoveredAmount() {
        return coveredAmount;
    }

    public BigDecimal getPatientPayAmount() {
        return patientPayAmount;
    }

    public PayoutRecipientType getPayoutRecipientType() {
        return payoutRecipientType;
    }

    public UUID getPolicyReservationId() {
        return policyReservationId;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public String getReason() {
        return reason;
    }

    public String getNotes() {
        return notes;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public Integer getFraudScore() {
        return fraudScore;
    }

    public String getFraudLevel() {
        return fraudLevel;
    }

    public Boolean getFraudPassed() {
        return fraudPassed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
