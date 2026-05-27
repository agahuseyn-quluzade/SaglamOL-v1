package az.saglamol.fraud.repository;

import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;
import az.saglamol.fraud.entity.OutboxEvent;

public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
