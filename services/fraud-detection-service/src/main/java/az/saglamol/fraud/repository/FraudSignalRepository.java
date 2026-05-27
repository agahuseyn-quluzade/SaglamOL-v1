package az.saglamol.fraud.repository;

import az.saglamol.fraud.entity.FraudSignal;
import az.saglamol.fraud.entity.FraudSignalType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudSignalRepository extends JpaRepository<FraudSignal, UUID> {
    List<FraudSignal> findByFraudAssessmentId(UUID fraudAssessmentId);

    long countBySignalType(FraudSignalType signalType);
}
