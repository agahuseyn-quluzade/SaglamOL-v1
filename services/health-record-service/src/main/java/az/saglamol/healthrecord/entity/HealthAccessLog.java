package az.saglamol.healthrecord.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "health_access_log")
public class HealthAccessLog {
    @Id
    private UUID id;
    private UUID recordId;
    private UUID actorId;
    private String action;
    private Instant createdAt;

    protected HealthAccessLog() {
    }
}
