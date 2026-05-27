package az.saglamol.notification.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.notification.dto.NotificationResponse;
import az.saglamol.notification.dto.SendNotificationRequest;
import az.saglamol.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public NotificationResponse send(@RequestBody SendNotificationRequest request) {
        return notificationService.send(request);
    }

    @GetMapping("/{id}")
    public NotificationResponse get(@PathVariable UUID id) {
        return notificationService.get(AuthContextHolder.getRequired(), id);
    }

    @GetMapping("/my")
    public List<NotificationResponse> my() {
        return notificationService.my(AuthContextHolder.getRequired());
    }

    @GetMapping("/by-user/{userId}")
    public List<NotificationResponse> byUser(@PathVariable UUID userId) {
        return notificationService.byUser(AuthContextHolder.getRequired(), userId);
    }

    @GetMapping("/by-company/{companyId}")
    public List<NotificationResponse> byCompany(@PathVariable UUID companyId) {
        return notificationService.byCompany(AuthContextHolder.getRequired(), companyId);
    }
}
