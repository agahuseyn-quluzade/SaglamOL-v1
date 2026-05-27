package az.saglamol.airisk.repository;

import az.saglamol.airisk.entity.OutboxEvent;
import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;

public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
