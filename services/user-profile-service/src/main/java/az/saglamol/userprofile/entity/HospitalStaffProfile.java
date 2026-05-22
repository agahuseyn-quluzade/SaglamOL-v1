package az.saglamol.userprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hospital_staff_profile")
public class HospitalStaffProfile {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(nullable = false, length = 120)
    private String position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HospitalStaffProfile() {
    }

    public HospitalStaffProfile(UUID id, UUID userId, UUID hospitalId, UUID branchId, String position, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.hospitalId = hospitalId;
        this.branchId = branchId;
        this.position = position;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public String getPosition() {
        return position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
