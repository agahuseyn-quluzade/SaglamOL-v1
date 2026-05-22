package az.saglamol.policy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "policy")
public class Policy {
    @Id
    private UUID id;
    private UUID patientId;
    private UUID planId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;

    protected Policy() {
    }
}
