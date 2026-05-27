package az.saglamol.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {
    @Id
    private UUID id;

    @Column(name = "template_code", nullable = false, length = 120)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "subject_template", length = 255)
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NotificationTemplate() {
    }

    public NotificationTemplate(UUID id, String templateCode, NotificationChannel channel, String subjectTemplate,
                                String bodyTemplate, TemplateStatus status, Instant createdAt) {
        this.id = id;
        this.templateCode = templateCode;
        this.channel = channel;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void update(String subjectTemplate, String bodyTemplate) {
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public void changeStatus(TemplateStatus status) {
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getTemplateCode() { return templateCode; }
    public NotificationChannel getChannel() { return channel; }
    public String getSubjectTemplate() { return subjectTemplate; }
    public String getBodyTemplate() { return bodyTemplate; }
    public TemplateStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
