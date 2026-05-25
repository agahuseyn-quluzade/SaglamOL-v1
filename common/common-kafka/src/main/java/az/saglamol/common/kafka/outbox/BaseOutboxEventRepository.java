package az.saglamol.common.kafka.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface BaseOutboxEventRepository<T extends BaseOutboxEvent> extends JpaRepository<T, UUID> {

    List<T> findTop100ByStatusAndNextRetryAtLessThanEqualAndRetryCountLessThanOrderByCreatedAtAsc(
            String status,
            Instant nextRetryAt,
            int maxRetries
    );
}
