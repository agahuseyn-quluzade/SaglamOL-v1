package az.saglamol.notification.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.notification.dto.TemplateRequest;
import az.saglamol.notification.dto.TemplateResponse;
import az.saglamol.notification.entity.NotificationTemplate;
import az.saglamol.notification.entity.TemplateStatus;
import az.saglamol.notification.exception.NotificationException;
import az.saglamol.notification.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationTemplateService {

    private final NotificationTemplateRepository repository;
    private final NotificationAccessService accessService;

    public NotificationTemplateService(NotificationTemplateRepository repository, NotificationAccessService accessService) {
        this.repository = repository;
        this.accessService = accessService;
    }

    @Transactional
    public TemplateResponse create(AuthContext authContext, TemplateRequest request) {
        accessService.requireAdmin(authContext);
        NotificationTemplate template = new NotificationTemplate(UUID.randomUUID(), request.templateCode(),
                request.channel(), request.subjectTemplate(), request.bodyTemplate(), TemplateStatus.ACTIVE, Instant.now());
        return toResponse(repository.save(template));
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> all(AuthContext authContext) {
        accessService.requireAdmin(authContext);
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public TemplateResponse update(AuthContext authContext, UUID templateId, TemplateRequest request) {
        accessService.requireAdmin(authContext);
        NotificationTemplate template = repository.findById(templateId)
                .orElseThrow(() -> new NotificationException("TEMPLATE_NOT_FOUND", "Template was not found"));
        template.update(request.subjectTemplate(), request.bodyTemplate());
        return toResponse(repository.save(template));
    }

    @Transactional
    public TemplateResponse changeStatus(AuthContext authContext, UUID templateId, TemplateStatus status) {
        accessService.requireAdmin(authContext);
        NotificationTemplate template = repository.findById(templateId)
                .orElseThrow(() -> new NotificationException("TEMPLATE_NOT_FOUND", "Template was not found"));
        template.changeStatus(status);
        return toResponse(repository.save(template));
    }

    private TemplateResponse toResponse(NotificationTemplate template) {
        return new TemplateResponse(template.getId(), template.getTemplateCode(), template.getChannel(),
                template.getSubjectTemplate(), template.getBodyTemplate(), template.getStatus(), template.getCreatedAt());
    }
}
