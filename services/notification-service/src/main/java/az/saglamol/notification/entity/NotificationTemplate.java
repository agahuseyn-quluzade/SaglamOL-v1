package az.saglamol.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "notification_template")
public class NotificationTemplate {
    @Id
    private UUID id;
    private String code;
    private String channel;
    private String body;

    protected NotificationTemplate() {
    }
}
