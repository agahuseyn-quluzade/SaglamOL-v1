package az.saglamol.claim.repository;

import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;

public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
