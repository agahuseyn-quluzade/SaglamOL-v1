package az.saglamol.claim.event.consumer;

import az.saglamol.claim.entity.ClaimStatusHistory;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.ClaimStatusHistoryRepository;
import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.KafkaTopics;
import az.saglamol.common.events.fraud.FraudCheckCompletedEvent;
import az.saglamol.common.kafka.consumer.IdempotentEventConsumer;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class FraudCheckConsumer extends IdempotentEventConsumer {

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository statusHistoryRepository;
    private final ObjectMapper objectMapper;

    public FraudCheckConsumer(
            ProcessedEventService processedEventService,
            ClaimRepository claimRepository,
            ClaimStatusHistoryRepository statusHistoryRepository,
            ObjectMapper objectMapper
    ) {
        super(processedEventService, "claim-service-fraud-check-consumer");
        this.claimRepository = claimRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.FRAUD_EVENTS, groupId = "${spring.kafka.consumer.group-id:claim-service-group}")
    public void onMessage(EventEnvelope<?> envelope) {
        consume(envelope);
    }

    @Override
    protected void handleEvent(EventEnvelope<?> envelope) {
        if (!FraudCheckCompletedEvent.class.getSimpleName().equals(envelope.eventType())) {
            return;
        }
        FraudCheckCompletedEvent event = objectMapper.convertValue(envelope.payload(), FraudCheckCompletedEvent.class);
        var claim = claimRepository.findById(event.claimId())
                .orElseThrow(() -> new ClaimException("CLAIM_NOT_FOUND", "Claim was not found"));
        Instant now = Instant.now();
        claim.updateFraud(toIntegerScore(event.fraudScore()), event.fraudLevel(), event.passed(), now);
        claimRepository.save(claim);
        statusHistoryRepository.save(new ClaimStatusHistory(
                UUID.randomUUID(),
                claim.getId(),
                claim.getStatus(),
                claim.getStatus(),
                null,
                "Fraud check completed",
                now
        ));
    }

    private Integer toIntegerScore(Double score) {
        if (score == null) {
            return null;
        }
        return (int) Math.round(score <= 1.0 ? score * 100 : score);
    }
}
