package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctor_profile")
public class DoctorProfile {
    @Id
    private UUID id;
    private UUID userId;
    private String licenseNo;
    private UUID hospitalId;
    private String specialty;
    private Instant createdAt;

    protected DoctorProfile() {
    }
}
