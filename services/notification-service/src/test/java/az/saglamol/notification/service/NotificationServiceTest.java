package az.saglamol.notification.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationTemplateRepository templateRepository = mock(NotificationTemplateRepository.class);
    private final NotificationRetryRepository retryRepository = mock(NotificationRetryRepository.class);
    private final NotificationSender sender = mock(NotificationSender.class);
    private NotificationService service;

    @BeforeEach
    void setUp() {
        when(sender.channel()).thenReturn(NotificationChannel.IN_APP);
        service = new NotificationService(notificationRepository, templateRepository, retryRepository,
                new TemplateRenderer(), List.of(sender), new NotificationAccessService());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(retryRepository.save(any(NotificationRetry.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sendCreatesRenderedNotificationAndMarksSent() {
        UUID userId = UUID.randomUUID();
        template("CLAIM_APPROVED", NotificationChannel.IN_APP, "Approved", "Claim approved for {{amount}}");

        var response = service.send(new SendNotificationRequest(userId, null, null, NotificationChannel.IN_APP,
                "CLAIM_APPROVED", Map.of("amount", "100.00"), "Claim", UUID.randomUUID(), null, null));

        assertEquals(NotificationStatus.SENT, response.status());
        assertEquals("Claim approved for 100.00", response.message());
    }

    @Test
    void failedRetryBecomesSentWhenSenderRecovers() {
        Notification failed = notification(NotificationStatus.FAILED, 1, 3, UUID.randomUUID());
        when(notificationRepository.findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, Integer.MAX_VALUE))
                .thenReturn(List.of(failed));

        service.retryFailedNotifications();

        assertEquals(NotificationStatus.SENT, failed.getStatus());
    }

    @Test
    void permanentlyFailedAfterMaxRetries() {
        Notification failed = notification(NotificationStatus.FAILED, 2, 3, UUID.randomUUID());
        org.mockito.Mockito.doThrow(new IllegalStateException("down")).when(sender).send(failed);
        when(notificationRepository.findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, Integer.MAX_VALUE))
                .thenReturn(List.of(failed));

        service.retryFailedNotifications();

        assertEquals(NotificationStatus.PERMANENTLY_FAILED, failed.getStatus());
        verify(retryRepository).save(any(NotificationRetry.class));
    }

    @Test
    void patientCanViewOwnNotificationOnly() {
        UUID userId = UUID.randomUUID();
        Notification own = notification(NotificationStatus.SENT, 0, 3, userId);
        when(notificationRepository.findById(own.getId())).thenReturn(Optional.of(own));

        service.get(auth(userId, RoleConstants.PATIENT, Map.of()), own.getId());

        NotificationException exception = assertThrows(NotificationException.class,
                () -> service.get(auth(UUID.randomUUID(), RoleConstants.PATIENT, Map.of()), own.getId()));
        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void insuranceAdminCompanyScopeApplies() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(notificationRepository.findByInsuranceCompanyIdOrderByCreatedAtDesc(companyId)).thenReturn(List.of());

        service.byCompany(auth(userId, RoleConstants.INSURANCE_ADMIN, Map.of("insuranceCompanyId", companyId.toString())), companyId);

        NotificationException exception = assertThrows(NotificationException.class,
                () -> service.byCompany(auth(userId, RoleConstants.INSURANCE_ADMIN,
                        Map.of("insuranceCompanyId", companyId.toString())), UUID.randomUUID()));
        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    private void template(String code, NotificationChannel channel, String subject, String body) {
        NotificationTemplate template = new NotificationTemplate(UUID.randomUUID(), code, channel, subject, body,
                TemplateStatus.ACTIVE, Instant.now());
        when(templateRepository.findByTemplateCodeAndChannelAndStatus(code, channel, TemplateStatus.ACTIVE))
                .thenReturn(Optional.of(template));
    }

    private Notification notification(NotificationStatus status, int retryCount, int maxRetries, UUID recipientUserId) {
        Instant now = Instant.now();
        return new Notification(UUID.randomUUID(), recipientUserId, null, null, NotificationChannel.IN_APP,
                "CLAIM_APPROVED", "Subject", "Message", status, retryCount, maxRetries,
                "Claim", UUID.randomUUID(), UUID.randomUUID(), null, now, null, now, null);
    }

    private AuthContext auth(UUID userId, String role, Map<String, String> headers) {
        return new AuthContext(userId, List.of(role), "corr", headers);
    }
}
