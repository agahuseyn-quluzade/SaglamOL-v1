package az.saglamol.notification.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.notification.dto.NotificationResponse;
import az.saglamol.notification.dto.SendNotificationRequest;
import az.saglamol.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notifications", description = "Notification sending and querying")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful notification operation")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    @Operation(summary = "Send notification request")
    public NotificationResponse send(@RequestBody SendNotificationRequest request) {
        return notificationService.send(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public NotificationResponse get(@PathVariable UUID id) {
        return notificationService.get(AuthContextHolder.getRequired(), id);
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's notifications")
    public List<NotificationResponse> my() {
        return notificationService.my(AuthContextHolder.getRequired());
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get notifications by user")
    public List<NotificationResponse> byUser(@PathVariable UUID userId) {
        return notificationService.byUser(AuthContextHolder.getRequired(), userId);
    }

    @GetMapping("/by-company/{companyId}")
    @Operation(summary = "Get notifications by company")
    public List<NotificationResponse> byCompany(@PathVariable UUID companyId) {
        return notificationService.byCompany(AuthContextHolder.getRequired(), companyId);
    }
}
