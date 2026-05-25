package az.saglamol.common.kafka.consumer;

import az.saglamol.common.events.EventEnvelope;
import org.springframework.transaction.annotation.Transactional;

public abstract class IdempotentEventConsumer {

    private final ProcessedEventService processedEventService;
    private final String consumerName;

    protected IdempotentEventConsumer(ProcessedEventService processedEventService, String consumerName) {
        this.processedEventService = processedEventService;
        this.consumerName = consumerName;
    }

    protected abstract void handleEvent(EventEnvelope<?> envelope);

    @Transactional
    public final void consume(EventEnvelope<?> envelope) {
        if (processedEventService.isAlreadyProcessed(envelope.eventId(), consumerName)) {
            return;
        }
        handleEvent(envelope);
        processedEventService.markProcessed(envelope.eventId(), envelope.eventType(), consumerName);
    }
}
