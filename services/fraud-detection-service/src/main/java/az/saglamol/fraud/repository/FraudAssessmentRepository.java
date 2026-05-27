package az.saglamol.fraud.repository;

import az.saglamol.fraud.entity.FraudAssessment;
import az.saglamol.fraud.entity.FraudAssessmentStatus;
import az.saglamol.fraud.entity.FraudLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudAssessmentRepository extends JpaRepository<FraudAssessment, UUID> {
    Optional<FraudAssessment> findTopByClaimIdOrderByCreatedAtDesc(UUID claimId);

    List<FraudAssessment> findByInsuranceCompanyId(UUID insuranceCompanyId);

    List<FraudAssessment> findByHospitalId(UUID hospitalId);

    long countByPatientProfileIdAndCreatedAtAfter(UUID patientProfileId, Instant createdAt);

    long countByHospitalIdAndFraudLevelInAndCreatedAtAfter(UUID hospitalId, List<FraudLevel> levels, Instant createdAt);

    long countByDoctorProfileIdAndFraudLevelInAndCreatedAtAfter(UUID doctorProfileId, List<FraudLevel> levels, Instant createdAt);

    long countByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, FraudAssessmentStatus status);

    long countByHospitalIdAndStatus(UUID hospitalId, FraudAssessmentStatus status);

    @Query("""
            select avg(f.fraudScore)
            from FraudAssessment f
            where f.insuranceCompanyId = :insuranceCompanyId
              and f.status = 'COMPLETED'
            """)
    BigDecimal averageScoreByCompany(UUID insuranceCompanyId);
}
