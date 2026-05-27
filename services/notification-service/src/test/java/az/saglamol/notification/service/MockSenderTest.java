package az.saglamol.notification.service;

import az.saglamol.notification.entity.Notification;
import az.saglamol.notification.entity.NotificationChannel;
import az.saglamol.notification.entity.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MockSenderTest {

    @Test
    void emailAndSmsMockSendersDoNotThrow() {
        Notification email = notification(NotificationChannel.EMAIL);
        Notification sms = notification(NotificationChannel.SMS);

        assertDoesNotThrow(() -> new EmailMockSender().send(email));
        assertDoesNotThrow(() -> new SmsMockSender().send(sms));
    }

    private Notification notification(NotificationChannel channel) {
        Instant now = Instant.now();
        return new Notification(UUID.randomUUID(), UUID.randomUUID(), "user@example.com", "+994501112233",
                channel, "PASSWORD_RESET", "Subject", "Message", NotificationStatus.PENDING, 0, 3,
                "User", UUID.randomUUID(), null, null, now, null, now, null);
    }
}
