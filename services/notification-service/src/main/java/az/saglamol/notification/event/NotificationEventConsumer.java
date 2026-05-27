package az.saglamol.notification.event;

import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.KafkaTopics;
import az.saglamol.common.events.claim.ClaimApprovedEvent;
import az.saglamol.common.events.claim.ClaimNeedsMoreDocumentsEvent;
import az.saglamol.common.events.claim.ClaimRejectedEvent;
import az.saglamol.common.events.claim.ClaimSubmittedEvent;
import az.saglamol.common.events.notification.NotificationRequestedEvent;
import az.saglamol.common.events.payment.PaymentCompletedEvent;
import az.saglamol.common.events.payment.PaymentFailedEvent;
import az.saglamol.common.events.policy.PolicyActivatedEvent;
import az.saglamol.common.events.policy.PolicyCreatedEvent;
import az.saglamol.common.events.profile.InsuranceCompanyCreatedEvent;
import az.saglamol.common.kafka.consumer.IdempotentEventConsumer;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.notification.dto.SendNotificationRequest;
import az.saglamol.notification.entity.NotificationChannel;
import az.saglamol.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventConsumer extends IdempotentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(
            ProcessedEventService processedEventService,
            NotificationService notificationService,
            ObjectMapper objectMapper
    ) {
        super(processedEventService, "notification-service-event-consumer");
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {
                    KafkaTopics.PROFILE_EVENTS,
                    KafkaTopics.POLICY_EVENTS,
                    KafkaTopics.PAYMENT_EVENTS,
                    KafkaTopics.CLAIM_EVENTS,
                    KafkaTopics.NOTIFICATION_EVENTS
            },
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}"
    )
    public void onMessage(EventEnvelope<?> envelope) {
        consume(envelope);
    }

    @Override
    protected void handleEvent(EventEnvelope<?> envelope) {
        String type = envelope.eventType();
        if (InsuranceCompanyCreatedEvent.class.getSimpleName().equals(type)) {
            InsuranceCompanyCreatedEvent event = convert(envelope, InsuranceCompanyCreatedEvent.class);
            notify("INSURANCE_COMPANY_CREATED", event.companyId(), "InsuranceCompany", event.companyId(),
                    event.companyId(), null, Map.of("companyName", event.name(), "taxId", event.taxId()));
        } else if (PolicyCreatedEvent.class.getSimpleName().equals(type)) {
            PolicyCreatedEvent event = convert(envelope, PolicyCreatedEvent.class);
            notify("POLICY_CREATED", event.patientProfileId(), "Policy", event.policyId(), event.companyId(),
                    null, Map.of("policyNumber", event.policyNumber(), "premiumAmount", event.premiumAmount()));
        } else if (PolicyActivatedEvent.class.getSimpleName().equals(type)) {
            PolicyActivatedEvent event = convert(envelope, PolicyActivatedEvent.class);
            notify("POLICY_ACTIVATED", event.patientProfileId(), "Policy", event.policyId(), event.companyId(),
                    null, Map.of("policyId", event.policyId()));
        } else if (PaymentCompletedEvent.class.getSimpleName().equals(type)) {
            PaymentCompletedEvent event = convert(envelope, PaymentCompletedEvent.class);
            notify("PAYMENT_COMPLETED", event.patientProfileId(), "Payment", event.paymentId(), event.companyId(),
                    event.hospitalId(), Map.of("paymentNumber", event.paymentNumber(), "amount", event.amount(), "currency", event.currency()));
        } else if (PaymentFailedEvent.class.getSimpleName().equals(type)) {
            PaymentFailedEvent event = convert(envelope, PaymentFailedEvent.class);
            notify("PAYMENT_FAILED", event.policyId(), "Payment", event.paymentId(), event.companyId(),
                    null, Map.of("reason", event.reason()));
        } else if (ClaimSubmittedEvent.class.getSimpleName().equals(type)) {
            ClaimSubmittedEvent event = convert(envelope, ClaimSubmittedEvent.class);
            notify("CLAIM_SUBMITTED", event.patientProfileId(), "Claim", event.claimId(), event.companyId(),
                    event.hospitalId(), Map.of("claimNumber", event.claimNumber(), "amount", event.totalAmount()));
        } else if (ClaimApprovedEvent.class.getSimpleName().equals(type)) {
            ClaimApprovedEvent event = convert(envelope, ClaimApprovedEvent.class);
            notify("CLAIM_APPROVED", event.patientProfileId(), "Claim", event.claimId(), event.companyId(),
                    null, Map.of("approvedAmount", event.approvedAmount()));
        } else if (ClaimRejectedEvent.class.getSimpleName().equals(type)) {
            ClaimRejectedEvent event = convert(envelope, ClaimRejectedEvent.class);
            notify("CLAIM_REJECTED", event.patientProfileId(), "Claim", event.claimId(), event.companyId(),
                    null, Map.of("reason", event.reason()));
        } else if (ClaimNeedsMoreDocumentsEvent.class.getSimpleName().equals(type)) {
            ClaimNeedsMoreDocumentsEvent event = convert(envelope, ClaimNeedsMoreDocumentsEvent.class);
            notify("CLAIM_NEEDS_MORE_DOCUMENTS", event.patientProfileId(), "Claim", event.claimId(), event.companyId(),
                    null, Map.of("reason", event.reason()));
        } else if (NotificationRequestedEvent.class.getSimpleName().equals(type)) {
            NotificationRequestedEvent event = convert(envelope, NotificationRequestedEvent.class);
            notificationService.sendFromEvent(new SendNotificationRequest(
                    event.recipientUserId(),
                    null,
                    null,
                    NotificationChannel.valueOf(event.channel()),
                    event.templateCode(),
                    event.variables(),
                    event.relatedEntityType(),
                    event.relatedEntityId(),
                    event.companyId(),
                    event.hospitalId()
            ));
        }
    }

    private <T> T convert(EventEnvelope<?> envelope, Class<T> type) {
        return objectMapper.convertValue(envelope.payload(), type);
    }

    private void notify(String templateCode, UUID recipientUserId, String relatedType, UUID relatedId,
                        UUID companyId, UUID hospitalId, Map<String, Object> variables) {
        notificationService.sendFromEvent(new SendNotificationRequest(
                recipientUserId,
                null,
                null,
                NotificationChannel.IN_APP,
                templateCode,
                variables,
                relatedType,
                relatedId,
                companyId,
                hospitalId
        ));
    }
}
