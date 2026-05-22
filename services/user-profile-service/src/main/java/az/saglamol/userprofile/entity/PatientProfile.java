package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patient_profile")
public class PatientProfile {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID iamUserId;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Gender gender;

    @Column(length = 32)
    private String phone;

    @Column(length = 320)
    private String email;

    @Column(name = "national_id", length = 80)
    private String nationalId;

    @Embedded
    private Address address;

    @Column(name = "emergency_contact_name", length = 160)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 32)
    private String emergencyContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status", nullable = false, length = 30)
    private ProfileStatus profileStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PatientProfile() {
    }

    public PatientProfile(UUID id, UUID iamUserId, String firstName, String lastName, LocalDate dateOfBirth,
                          Gender gender, String phone, String email, String nationalId, Address address,
                          String emergencyContactName, String emergencyContactPhone, ProfileStatus profileStatus,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.iamUserId = iamUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.nationalId = nationalId;
        this.address = address;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.profileStatus = profileStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(String firstName, String lastName, LocalDate dateOfBirth, Gender gender, String phone,
                       String email, String nationalId, Address address, String emergencyContactName,
                       String emergencyContactPhone, ProfileStatus profileStatus, Instant updatedAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.nationalId = nationalId;
        this.address = address;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getNationalId() {
        return nationalId;
    }

    public Address getAddress() {
        return address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
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
