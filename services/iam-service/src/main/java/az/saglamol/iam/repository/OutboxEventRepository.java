package az.saglamol.iam.repository;

import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;
import az.saglamol.iam.entity.OutboxEvent;

public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
