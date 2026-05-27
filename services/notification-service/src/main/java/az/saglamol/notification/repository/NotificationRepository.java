package az.saglamol.notification.repository;

import az.saglamol.notification.entity.Notification;
import az.saglamol.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);

    List<Notification> findByInsuranceCompanyIdOrderByCreatedAtDesc(UUID insuranceCompanyId);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}
