package az.saglamol.claim.event.consumer;

import az.saglamol.claim.entity.ClaimStatusHistory;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.ClaimStatusHistoryRepository;
import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.KafkaTopics;
import az.saglamol.common.events.risk.RiskAnalysisCompletedEvent;
import az.saglamol.common.kafka.consumer.IdempotentEventConsumer;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RiskAnalysisConsumer extends IdempotentEventConsumer {

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository statusHistoryRepository;
    private final ObjectMapper objectMapper;

    public RiskAnalysisConsumer(
            ProcessedEventService processedEventService,
            ClaimRepository claimRepository,
            ClaimStatusHistoryRepository statusHistoryRepository,
            ObjectMapper objectMapper
    ) {
        super(processedEventService, "claim-service-risk-analysis-consumer");
        this.claimRepository = claimRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.RISK_EVENTS, groupId = "${spring.kafka.consumer.group-id:claim-service-group}")
    public void onMessage(EventEnvelope<?> envelope) {
        consume(envelope);
    }

    @Override
    protected void handleEvent(EventEnvelope<?> envelope) {
        if (!RiskAnalysisCompletedEvent.class.getSimpleName().equals(envelope.eventType())) {
            return;
        }
        RiskAnalysisCompletedEvent event = objectMapper.convertValue(envelope.payload(), RiskAnalysisCompletedEvent.class);
        var claim = claimRepository.findById(event.claimId())
                .orElseThrow(() -> new ClaimException("CLAIM_NOT_FOUND", "Claim was not found"));
        Instant now = Instant.now();
        claim.updateRisk(toIntegerScore(event.score()), event.level(), now);
        claimRepository.save(claim);
        statusHistoryRepository.save(new ClaimStatusHistory(
                UUID.randomUUID(),
                claim.getId(),
                claim.getStatus(),
                claim.getStatus(),
                null,
                "Risk analysis completed",
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
