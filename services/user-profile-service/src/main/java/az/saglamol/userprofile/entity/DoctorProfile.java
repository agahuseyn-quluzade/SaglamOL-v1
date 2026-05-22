package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private UUID iamUserId;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "license_no", nullable = false, unique = true, length = 120)
    private String licenseNumber;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(length = 160)
    private String specialty;

    @Column(length = 32)
    private String phone;

    @Column(length = 320)
    private String email;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status", nullable = false, length = 30)
    private ProfileStatus profileStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DoctorProfile() {
    }

    public DoctorProfile(UUID id, UUID iamUserId, String firstName, String lastName, String licenseNumber,
                         UUID hospitalId, String specialty, String phone, String email, Address address,
                         ProfileStatus profileStatus, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.iamUserId = iamUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
        this.hospitalId = hospitalId;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.profileStatus = profileStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(String firstName, String lastName, String licenseNumber, UUID hospitalId, String specialty,
                       String phone, String email, Address address, ProfileStatus profileStatus, Instant updatedAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
        this.hospitalId = hospitalId;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.profileStatus = profileStatus;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return iamUserId;
    }

    public UUID getIamUserId() {
        return iamUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLicenseNo() {
        return licenseNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public ProfileStatus getProfileStatus() {
        return profileStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
