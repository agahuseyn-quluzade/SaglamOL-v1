package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctor_profile")
public class DoctorProfile {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "license_no", nullable = false, unique = true, length = 120)
    private String licenseNo;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(length = 160)
    private String specialty;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DoctorProfile() {
    }

    public DoctorProfile(UUID id, UUID userId, String licenseNo, UUID hospitalId, String specialty, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.licenseNo = licenseNo;
        this.hospitalId = hospitalId;
        this.specialty = specialty;
        this.createdAt = createdAt;
    }

    public void update(String licenseNo, UUID hospitalId, String specialty) {
        this.licenseNo = licenseNo;
        this.hospitalId = hospitalId;
        this.specialty = specialty;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
