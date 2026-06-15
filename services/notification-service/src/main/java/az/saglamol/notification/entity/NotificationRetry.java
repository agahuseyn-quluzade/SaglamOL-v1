package az.saglamol.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_retry")
public class NotificationRetry {
    @Id
    private UUID id;
    private UUID notificationId;
    private Integer retryCount;
    private Instant nextRetryAt;

    protected NotificationRetry() {
    }
}
