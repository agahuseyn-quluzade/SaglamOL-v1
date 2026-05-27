package az.saglamol.userprofile.repository;

import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;
import az.saglamol.userprofile.entity.OutboxEvent;

public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
