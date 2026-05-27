package az.saglamol.claim.event.consumer;

import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.ClaimStatusHistory;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.ClaimStatusHistoryRepository;
import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.KafkaTopics;
import az.saglamol.common.events.claim.ClaimPaidEvent;
import az.saglamol.common.events.payment.ClaimPayoutCompletedEvent;
import az.saglamol.common.events.payment.ClaimPayoutFailedEvent;
import az.saglamol.common.kafka.consumer.IdempotentEventConsumer;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentConsumer extends IdempotentEventConsumer {

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository statusHistoryRepository;
    private final OutboxEventService<OutboxEvent> outboxEventService;
    private final ObjectMapper objectMapper;

    public PaymentConsumer(
            ProcessedEventService processedEventService,
            ClaimRepository claimRepository,
            ClaimStatusHistoryRepository statusHistoryRepository,
            OutboxEventService<OutboxEvent> outboxEventService,
            ObjectMapper objectMapper
    ) {
        super(processedEventService, "claim-service-payment-consumer");
        this.claimRepository = claimRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.outboxEventService = outboxEventService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "${spring.kafka.consumer.group-id:claim-service-group}")
    public void onMessage(EventEnvelope<?> envelope) {
        consume(envelope);
    }

    @Override
    protected void handleEvent(EventEnvelope<?> envelope) {
        if (ClaimPayoutCompletedEvent.class.getSimpleName().equals(envelope.eventType())) {
            handlePayoutCompleted(objectMapper.convertValue(envelope.payload(), ClaimPayoutCompletedEvent.class));
            return;
        }
        if (ClaimPayoutFailedEvent.class.getSimpleName().equals(envelope.eventType())) {
            handlePayoutFailed(objectMapper.convertValue(envelope.payload(), ClaimPayoutFailedEvent.class));
        }
    }

    private void handlePayoutCompleted(ClaimPayoutCompletedEvent event) {
        var claim = claimRepository.findById(event.claimId())
                .orElseThrow(() -> new ClaimException("CLAIM_NOT_FOUND", "Claim was not found"));
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            return;
        }
        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        claim.markPaid(now);
        claimRepository.save(claim);
        statusHistoryRepository.save(new ClaimStatusHistory(
                UUID.randomUUID(),
                claim.getId(),
                fromStatus,
                ClaimStatus.PAID,
                null,
                "Claim payout completed",
                now
        ));
        outboxEventService.saveEvent("Claim", claim.getId(), ClaimPaidEvent.class.getSimpleName(), new ClaimPaidEvent(
                claim.getId(),
                claim.getInsuranceCompanyId(),
                claim.getPatientProfileId(),
                event.paidAmount(),
                now
        ));
    }

    private void handlePayoutFailed(ClaimPayoutFailedEvent event) {
        var claim = claimRepository.findById(event.claimId())
                .orElseThrow(() -> new ClaimException("CLAIM_NOT_FOUND", "Claim was not found"));
        if (claim.getStatus() == ClaimStatus.PAID || claim.getStatus() == ClaimStatus.CANCELLED) {
            return;
        }
        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        claim.markPayoutFailed(event.reason(), now);
        claimRepository.save(claim);
        statusHistoryRepository.save(new ClaimStatusHistory(
                UUID.randomUUID(),
                claim.getId(),
                fromStatus,
                ClaimStatus.PAYOUT_FAILED,
                null,
                event.reason(),
                now
        ));
    }
}
