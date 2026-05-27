package az.saglamol.healthrecord.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "health_records")
public class HealthRecord {
    @Id
    private UUID id;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "doctor_profile_id")
    private UUID doctorProfileId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 30)
    private VisitType visitType;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 40)
    private RecordType recordType;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HealthRecordStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected HealthRecord() {
    }

    public HealthRecord(UUID id, UUID patientProfileId, UUID doctorProfileId, UUID hospitalId, UUID branchId,
                        UUID claimId, LocalDate visitDate, VisitType visitType, RecordType recordType,
                        String diagnosis, String notes, HealthRecordStatus status,
                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.patientProfileId = patientProfileId;
        this.doctorProfileId = doctorProfileId;
        this.hospitalId = hospitalId;
        this.branchId = branchId;
        this.claimId = claimId;
        this.visitDate = visitDate;
        this.visitType = visitType;
        this.recordType = recordType;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void archive(Instant updatedAt) {
        this.status = HealthRecordStatus.ARCHIVED;
        this.updatedAt = updatedAt;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public UUID getDoctorProfileId() {
        return doctorProfileId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public VisitType getVisitType() {
        return visitType;
    }

    public RecordType getRecordType() {
        return recordType;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getNotes() {
        return notes;
    }

    public HealthRecordStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
