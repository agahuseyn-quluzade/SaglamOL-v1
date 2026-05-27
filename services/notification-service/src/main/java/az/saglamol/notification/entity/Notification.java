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
@Table(name = "notifications")
public class Notification {
    @Id
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "template_code", nullable = false, length = 120)
    private String templateCode;

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "related_entity_type", length = 80)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "insurance_company_id")
    private UUID insuranceCompanyId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected Notification() {
    }

    public Notification(UUID id, UUID recipientUserId, String recipientEmail, String recipientPhone,
                        NotificationChannel channel, String templateCode, String subject, String message,
                        NotificationStatus status, int retryCount, int maxRetries, String relatedEntityType,
                        UUID relatedEntityId, UUID insuranceCompanyId, UUID hospitalId, Instant createdAt,
                        Instant sentAt, Instant updatedAt, String errorMessage) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.recipientEmail = recipientEmail;
        this.recipientPhone = recipientPhone;
        this.channel = channel;
        this.templateCode = templateCode;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.hospitalId = hospitalId;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.updatedAt = updatedAt;
        this.errorMessage = errorMessage;
    }

    public void markSent(Instant now) {
        this.status = NotificationStatus.SENT;
        this.sentAt = now;
        this.updatedAt = now;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage, Instant now) {
        this.status = NotificationStatus.FAILED;
        this.retryCount++;
        this.updatedAt = now;
        this.errorMessage = errorMessage;
    }

    public void markPermanentlyFailed(String errorMessage, Instant now) {
        this.status = NotificationStatus.PERMANENTLY_FAILED;
        this.updatedAt = now;
        this.errorMessage = errorMessage;
    }

    public UUID getId() { return id; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getRecipientPhone() { return recipientPhone; }
    public NotificationChannel getChannel() { return channel; }
    public String getTemplateCode() { return templateCode; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public NotificationStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public UUID getRelatedEntityId() { return relatedEntityId; }
    public UUID getInsuranceCompanyId() { return insuranceCompanyId; }
    public UUID getHospitalId() { return hospitalId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getErrorMessage() { return errorMessage; }
}
