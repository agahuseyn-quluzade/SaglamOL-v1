package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
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
    private UUID userId;

    @Column(name = "employee_no", nullable = false, unique = true, length = 120)
    private String employeeNo;

    @Column(length = 160)
    private String department;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AgentProfile() {
    }

    public AgentProfile(UUID id, UUID userId, String employeeNo, String department, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.employeeNo = employeeNo;
        this.department = department;
        this.createdAt = createdAt;
    }

    public void update(String employeeNo, String department) {
        this.employeeNo = employeeNo;
        this.department = department;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public String getDepartment() {
        return department;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
