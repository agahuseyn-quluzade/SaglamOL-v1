package az.saglamol.userprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hospital_branch")
public class HospitalBranch {

    @Id
    private UUID id;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 255)
    private String addressLine;

    @Column(length = 80)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HospitalBranch() {
    }

    public HospitalBranch(UUID id, UUID hospitalId, String name, String city, String addressLine, String phone,
                          Instant createdAt) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.name = name;
        this.city = city;
        this.addressLine = addressLine;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
