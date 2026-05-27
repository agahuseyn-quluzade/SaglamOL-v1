package az.saglamol.airisk.repository;

import az.saglamol.airisk.entity.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, UUID> {
}
