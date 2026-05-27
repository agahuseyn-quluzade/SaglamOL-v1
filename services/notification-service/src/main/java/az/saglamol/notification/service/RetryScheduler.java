package az.saglamol.notification.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {

    private final NotificationService notificationService;

    public RetryScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${notification.retry.interval-ms:60000}")
    public void retryFailed() {
        notificationService.retryFailedNotifications();
    }
}
