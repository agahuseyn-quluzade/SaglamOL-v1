package az.saglamol.notification.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.notification.dto.TemplateRequest;
import az.saglamol.notification.dto.TemplateResponse;
import az.saglamol.notification.entity.TemplateStatus;
import az.saglamol.notification.service.NotificationTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications/templates")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    public NotificationTemplateController(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public TemplateResponse create(@RequestBody TemplateRequest request) {
        return templateService.create(AuthContextHolder.getRequired(), request);
    }

    @GetMapping
    public List<TemplateResponse> all() {
        return templateService.all(AuthContextHolder.getRequired());
    }

    @PutMapping("/{id}")
    public TemplateResponse update(@PathVariable UUID id, @RequestBody TemplateRequest request) {
        return templateService.update(AuthContextHolder.getRequired(), id, request);
    }

    @PatchMapping("/{id}/status")
    public TemplateResponse status(@PathVariable UUID id, @RequestParam TemplateStatus status) {
        return templateService.changeStatus(AuthContextHolder.getRequired(), id, status);
    }
}
