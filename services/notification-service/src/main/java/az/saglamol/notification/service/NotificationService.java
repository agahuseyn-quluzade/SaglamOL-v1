package az.saglamol.notification.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.notification.dto.NotificationResponse;
import az.saglamol.notification.dto.SendNotificationRequest;
import az.saglamol.notification.entity.Notification;
import az.saglamol.notification.entity.NotificationChannel;
import az.saglamol.notification.entity.NotificationRetry;
import az.saglamol.notification.entity.NotificationStatus;
import az.saglamol.notification.entity.NotificationTemplate;
import az.saglamol.notification.entity.TemplateStatus;
import az.saglamol.notification.exception.NotificationException;
import az.saglamol.notification.repository.NotificationRepository;
import az.saglamol.notification.repository.NotificationRetryRepository;
import az.saglamol.notification.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationRetryRepository retryRepository;
    private final TemplateRenderer renderer;
    private final Map<NotificationChannel, NotificationSender> senders;
    private final NotificationAccessService accessService;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationTemplateRepository templateRepository,
            NotificationRetryRepository retryRepository,
            TemplateRenderer renderer,
            List<NotificationSender> senders,
            NotificationAccessService accessService
    ) {
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
        this.retryRepository = retryRepository;
        this.renderer = renderer;
        this.senders = senders.stream().collect(Collectors.toMap(NotificationSender::channel, Function.identity()));
        this.accessService = accessService;
    }

    @Transactional
    public NotificationResponse send(SendNotificationRequest request) {
        Notification notification = createPending(request);
        return toResponse(sendPending(notification));
    }

    @Transactional
    public Notification sendFromEvent(SendNotificationRequest request) {
        return sendPending(createPending(request));
    }

    @Transactional
    public void retryFailedNotifications() {
        notificationRepository.findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, Integer.MAX_VALUE).stream()
                .filter(notification -> notification.getRetryCount() < notification.getMaxRetries())
                .forEach(this::sendPending);
        notificationRepository.findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, Integer.MAX_VALUE).stream()
                .filter(notification -> notification.getRetryCount() >= notification.getMaxRetries())
                .forEach(notification -> {
                    notification.markPermanentlyFailed(notification.getErrorMessage(), Instant.now());
                    notificationRepository.save(notification);
                });
    }

    @Transactional(readOnly = true)
    public NotificationResponse get(AuthContext authContext, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException("NOTIFICATION_NOT_FOUND", "Notification was not found"));
        accessService.requireCanView(authContext, notification);
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> my(AuthContext authContext) {
        return byUser(authContext, authContext.userId());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> byUser(AuthContext authContext, UUID userId) {
        accessService.requireCanViewUser(authContext, userId);
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> byCompany(AuthContext authContext, UUID companyId) {
        UUID scopedCompanyId = accessService.scopedCompany(authContext, companyId);
        return notificationRepository.findByInsuranceCompanyIdOrderByCreatedAtDesc(scopedCompanyId).stream().map(this::toResponse).toList();
    }

    private Notification createPending(SendNotificationRequest request) {
        NotificationTemplate template = templateRepository
                .findByTemplateCodeAndChannelAndStatus(request.templateCode(), request.channel(), TemplateStatus.ACTIVE)
                .orElseThrow(() -> new NotificationException("TEMPLATE_NOT_FOUND", "Active notification template was not found"));
        Instant now = Instant.now();
        Notification notification = new Notification(
                UUID.randomUUID(),
                request.recipientUserId(),
                request.recipientEmail(),
                request.recipientPhone(),
                request.channel(),
                request.templateCode(),
                renderer.render(template.getSubjectTemplate(), request.variables()),
                renderer.render(template.getBodyTemplate(), request.variables()),
                NotificationStatus.PENDING,
                0,
                3,
                request.relatedEntityType(),
                request.relatedEntityId(),
                request.insuranceCompanyId(),
                request.hospitalId(),
                now,
                null,
                now,
                null
        );
        return notificationRepository.save(notification);
    }

    private Notification sendPending(Notification notification) {
        try {
            NotificationSender sender = senders.get(notification.getChannel());
            if (sender == null) {
                throw new NotificationException("SENDER_NOT_FOUND", "Sender was not found for channel " + notification.getChannel());
            }
            sender.send(notification);
            notification.markSent(Instant.now());
        } catch (RuntimeException exception) {
            notification.markFailed(exception.getMessage(), Instant.now());
            retryRepository.save(new NotificationRetry(
                    UUID.randomUUID(),
                    notification.getId(),
                    notification.getRetryCount(),
                    exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage(),
                    Instant.now()
            ));
            if (notification.getRetryCount() >= notification.getMaxRetries()) {
                notification.markPermanentlyFailed(notification.getErrorMessage(), Instant.now());
            }
        }
        return notificationRepository.save(notification);
    }

    NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getRecipientEmail(),
                notification.getRecipientPhone(),
                notification.getChannel(),
                notification.getTemplateCode(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getRetryCount(),
                notification.getMaxRetries(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.getInsuranceCompanyId(),
                notification.getHospitalId(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getUpdatedAt(),
                notification.getErrorMessage()
        );
    }
}
