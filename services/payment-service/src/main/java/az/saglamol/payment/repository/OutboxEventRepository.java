package az.saglamol.payment.repository;

import az.saglamol.common.kafka.outbox.BaseOutboxEventRepository;
import az.saglamol.payment.entity.OutboxEvent;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends BaseOutboxEventRepository<OutboxEvent> {
}
