package az.saglamol.payment.event;

import az.saglamol.common.events.EventEnvelope;
import az.saglamol.common.events.KafkaTopics;
import az.saglamol.common.events.claim.ClaimApprovedEvent;
import az.saglamol.common.kafka.consumer.IdempotentEventConsumer;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClaimApprovedConsumer extends IdempotentEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public ClaimApprovedConsumer(
            ProcessedEventService processedEventService,
            PaymentService paymentService,
            ObjectMapper objectMapper
    ) {
        super(processedEventService, "payment-service-claim-approved-consumer");
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.CLAIM_EVENTS, groupId = "${spring.kafka.consumer.group-id:payment-service-group}")
    public void onMessage(EventEnvelope<?> envelope) {
        consume(envelope);
    }

    @Override
    protected void handleEvent(EventEnvelope<?> envelope) {
        if (!ClaimApprovedEvent.class.getSimpleName().equals(envelope.eventType())) {
            return;
        }
        paymentService.createClaimPayoutFromApprovedClaim(objectMapper.convertValue(envelope.payload(), ClaimApprovedEvent.class));
    }
}
