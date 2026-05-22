package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_profile")
public class AgentProfile {
    @Id
    private UUID id;
    private UUID userId;
    private String employeeNo;
    private String department;
    private Instant createdAt;

    protected AgentProfile() {
    }
}
