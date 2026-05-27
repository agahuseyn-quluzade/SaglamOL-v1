package az.saglamol.healthrecord.repository;

import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;
import az.saglamol.healthrecord.entity.OutboxEvent;

public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
