package az.saglamol.common.events;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String schemaVersion,
        String aggregateType,
        String aggregateId,
        Instant occurredAt,
        String producer,
        String correlationId,
        T payload
) {
}
