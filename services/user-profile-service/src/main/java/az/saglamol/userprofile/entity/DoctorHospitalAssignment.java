package az.saglamol.userprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctor_hospital_assignment")
public class DoctorHospitalAssignment {

    @Id
    private UUID id;

    @Column(name = "doctor_profile_id", nullable = false)
    private UUID doctorProfileId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(length = 120)
    private String department;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryAssignment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DoctorHospitalAssignment() {
    }

    public DoctorHospitalAssignment(UUID id, UUID doctorProfileId, UUID hospitalId, UUID branchId, String department,
                                    boolean primaryAssignment, Instant createdAt) {
        this.id = id;
        this.doctorProfileId = doctorProfileId;
        this.hospitalId = hospitalId;
        this.branchId = branchId;
        this.department = department;
        this.primaryAssignment = primaryAssignment;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
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

    public String getDepartment() {
        return department;
    }

    public boolean isPrimaryAssignment() {
        return primaryAssignment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
