package az.saglamol.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class Notification {
    @Id
    private UUID id;
    private UUID recipientUserId;
    private String channel;
    private String status;
    private String subject;
    private String body;
    private Instant createdAt;

    protected Notification() {
    }
}
