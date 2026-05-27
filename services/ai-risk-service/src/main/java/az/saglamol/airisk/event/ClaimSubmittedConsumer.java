package az.saglamol.airisk.event;

import az.saglamol.airisk.client.ClaimDetailResponse;
import az.saglamol.airisk.service.AiRiskAssessmentService;
import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.KafkaTopics;
import az.saglamol.common.events.claim.ClaimSubmittedEvent;
import az.saglamol.common.kafka.consumer.IdempotentEventConsumer;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClaimSubmittedConsumer extends IdempotentEventConsumer {

    private final AiRiskAssessmentService assessmentService;
    private final ObjectMapper objectMapper;

    public ClaimSubmittedConsumer(
            ProcessedEventService processedEventService,
            AiRiskAssessmentService assessmentService,
            ObjectMapper objectMapper
    ) {
        super(processedEventService, "ai-risk-claim-submitted-consumer");
        this.assessmentService = assessmentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.CLAIM_EVENTS, groupId = "${spring.kafka.consumer.group-id:ai-risk-service-group}")
    public void onMessage(EventEnvelope<?> envelope) {
        consume(envelope);
    }

    @Override
    protected void handleEvent(EventEnvelope<?> envelope) {
        if (!ClaimSubmittedEvent.class.getSimpleName().equals(envelope.eventType())) {
            return;
        }
        ClaimSubmittedEvent event = objectMapper.convertValue(envelope.payload(), ClaimSubmittedEvent.class);
        assessmentService.assessSubmittedClaim(new ClaimDetailResponse(
                event.claimId(),
                event.claimNumber(),
                event.policyId(),
                event.companyId(),
                event.patientProfileId(),
                event.hospitalId(),
                event.doctorProfileId(),
                "SUBMITTED",
                event.serviceType(),
                null,
                event.totalAmount()
        ));
    }
}
