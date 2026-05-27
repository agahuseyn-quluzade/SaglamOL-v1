package az.saglamol.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_retries")
public class NotificationRetry {
    @Id
    private UUID id;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "failure_reason", nullable = false, columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    protected NotificationRetry() {
    }

    public NotificationRetry(UUID id, UUID notificationId, int attemptNumber, String failureReason, Instant attemptedAt) {
        this.id = id;
        this.notificationId = notificationId;
        this.attemptNumber = attemptNumber;
        this.failureReason = failureReason;
        this.attemptedAt = attemptedAt;
    }

    public UUID getId() { return id; }
    public UUID getNotificationId() { return notificationId; }
    public int getAttemptNumber() { return attemptNumber; }
    public String getFailureReason() { return failureReason; }
    public Instant getAttemptedAt() { return attemptedAt; }
}
