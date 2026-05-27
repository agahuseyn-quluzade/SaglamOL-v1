package az.saglamol.airisk.repository;

import az.saglamol.airisk.entity.AiRiskAssessment;
import az.saglamol.airisk.entity.AiRiskAssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiRiskAssessmentRepository extends JpaRepository<AiRiskAssessment, UUID> {
    Optional<AiRiskAssessment> findTopByClaimIdOrderByCreatedAtDesc(UUID claimId);

    List<AiRiskAssessment> findByInsuranceCompanyId(UUID insuranceCompanyId);

    long countByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, AiRiskAssessmentStatus status);
}
