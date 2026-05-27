package az.saglamol.notification.repository;

import az.saglamol.notification.entity.NotificationChannel;
import az.saglamol.notification.entity.NotificationTemplate;
import az.saglamol.notification.entity.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    Optional<NotificationTemplate> findByTemplateCodeAndChannelAndStatus(String templateCode, NotificationChannel channel, TemplateStatus status);

    Optional<NotificationTemplate> findByTemplateCodeAndChannel(String templateCode, NotificationChannel channel);

    List<NotificationTemplate> findByTemplateCode(String templateCode);
}
