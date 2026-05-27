package az.saglamol.notification.repository;

import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;
import az.saglamol.notification.entity.OutboxEvent;

public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
