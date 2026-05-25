package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_profile")
public class AgentProfile {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID iamUserId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "employee_no", nullable = false, unique = true, length = 120)
    private String employeeCode;

    @Column(length = 160)
    private String department;

    @Column(length = 32)
    private String phone;

    @Column(length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status", nullable = false, length = 30)
    private ProfileStatus profileStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AgentProfile() {
    }

    public AgentProfile(UUID id, UUID iamUserId, String firstName, String lastName, String employeeCode,
                        String department, String phone, String email, ProfileStatus profileStatus,
                        Instant createdAt, Instant updatedAt) {
        this(id, iamUserId, null, firstName, lastName, employeeCode, department, phone, email, profileStatus, createdAt, updatedAt);
    }

    public AgentProfile(UUID id, UUID iamUserId, UUID insuranceCompanyId, String firstName, String lastName,
                        String employeeCode, String department, String phone, String email,
                        ProfileStatus profileStatus, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.iamUserId = iamUserId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeCode = employeeCode;
        this.department = department;
        this.phone = phone;
        this.email = email;
        this.profileStatus = profileStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(UUID insuranceCompanyId, String firstName, String lastName, String employeeCode, String department, String phone,
                       String email, ProfileStatus profileStatus, Instant updatedAt) {
        this.insuranceCompanyId = insuranceCompanyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeCode = employeeCode;
        this.department = department;
        this.phone = phone;
        this.email = email;
        this.profileStatus = profileStatus;
        this.updatedAt = updatedAt;
    }

    public void linkToCompany(UUID insuranceCompanyId, Instant updatedAt) {
        this.insuranceCompanyId = insuranceCompanyId;
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

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmployeeNo() {
        return employeeCode;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getDepartment() {
        return department;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
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
