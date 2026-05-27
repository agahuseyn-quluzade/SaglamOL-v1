package az.saglamol.notification.service;

import az.saglamol.notification.entity.Notification;
import az.saglamol.notification.entity.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailMockSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailMockSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        log.info("Mock email notification id={} to={} subject={}",
                notification.getId(), notification.getRecipientEmail(), notification.getSubject());
    }
}
