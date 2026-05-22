package az.saglamol.healthrecord.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "health_record")
public class HealthRecord {
    @Id
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private String diagnosisCode;
    private LocalDate treatmentDate;
    private Instant createdAt;

    protected HealthRecord() {
    }
}
