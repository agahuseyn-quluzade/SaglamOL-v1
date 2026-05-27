package az.saglamol.healthrecord.repository;

import az.saglamol.healthrecord.entity.HealthAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HealthAccessLogRepository extends JpaRepository<HealthAccessLog, UUID> {
    List<HealthAccessLog> findByHealthRecordId(UUID healthRecordId);

    List<HealthAccessLog> findByDocumentId(UUID documentId);

    List<HealthAccessLog> findByAccessedByUserId(UUID accessedByUserId);
}
