package az.saglamol.userprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "insurance_companies")
public class InsuranceCompany {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "tax_id", nullable = false, unique = true, length = 80)
    private String taxId;

    @Column(name = "license_number", nullable = false, unique = true, length = 120)
    private String licenseNumber;

    @Column(length = 320)
    private String email;

    @Column(length = 32)
    private String phone;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InsuranceCompanyStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InsuranceCompany() {
    }

    public InsuranceCompany(UUID id, String name, String taxId, String licenseNumber, String email, String phone,
                            Address address, InsuranceCompanyStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.taxId = taxId;
        this.licenseNumber = licenseNumber;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(String name, String email, String phone, Address address, Instant updatedAt) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(InsuranceCompanyStatus status, Instant updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
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

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }

    public InsuranceCompanyStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
