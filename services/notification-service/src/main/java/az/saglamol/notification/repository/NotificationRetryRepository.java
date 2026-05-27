package az.saglamol.notification.repository;

import az.saglamol.notification.entity.NotificationRetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRetryRepository extends JpaRepository<NotificationRetry, UUID> {
}
