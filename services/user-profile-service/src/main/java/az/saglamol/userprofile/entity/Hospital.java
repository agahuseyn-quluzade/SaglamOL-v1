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
@Table(name = "hospital")
public class Hospital {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "tax_id", nullable = false, unique = true, length = 80)
    private String taxId;

    @Column(name = "license_no", nullable = false, unique = true, length = 120)
    private String licenseNo;

    @Column(length = 80)
    private String phone;

    @Column(length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HospitalStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Hospital() {
    }

    public Hospital(UUID id, String name, String taxId, String licenseNo, String phone, String email,
                    HospitalStatus status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.taxId = taxId;
        this.licenseNo = licenseNo;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void update(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public void changeStatus(HospitalStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public HospitalStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
