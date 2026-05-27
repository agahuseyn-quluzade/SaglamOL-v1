package az.saglamol.notification.service;

import az.saglamol.notification.entity.Notification;
import az.saglamol.notification.entity.NotificationChannel;

public interface NotificationSender {
    NotificationChannel channel();

    void send(Notification notification);
}
