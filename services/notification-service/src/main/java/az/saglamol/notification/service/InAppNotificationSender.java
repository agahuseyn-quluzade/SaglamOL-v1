package az.saglamol.notification.service;

import az.saglamol.notification.entity.Notification;
import az.saglamol.notification.entity.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void send(Notification notification) {
        log.info("In-app notification id={} user={}", notification.getId(), notification.getRecipientUserId());
    }
}
