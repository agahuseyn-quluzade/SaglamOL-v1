package az.saglamol.userprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "insurance_company_staff_profiles")
public class InsuranceCompanyStaffProfile {

    @Id
    private UUID id;

    @Column(name = "iam_user_id", nullable = false)
    private UUID iamUserId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 40)
    private InsuranceCompanyStaffRoleType roleType;

    @Column(length = 120)
    private String position;

    @Column(name = "employee_code", length = 120)
    private String employeeCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InsuranceCompanyStaffStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InsuranceCompanyStaffProfile() {
    }

    public InsuranceCompanyStaffProfile(UUID id, UUID iamUserId, UUID insuranceCompanyId,
                                        InsuranceCompanyStaffRoleType roleType, String position,
                                        String employeeCode, InsuranceCompanyStaffStatus status,
                                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.iamUserId = iamUserId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.roleType = roleType;
        this.position = position;
        this.employeeCode = employeeCode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(InsuranceCompanyStaffStatus status, Instant updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIamUserId() {
        return iamUserId;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public InsuranceCompanyStaffRoleType getRoleType() {
        return roleType;
    }

    public String getPosition() {
        return position;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public InsuranceCompanyStaffStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
