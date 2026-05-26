package az.saglamol.policy.repository;

import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByInsuranceCompanyId(UUID insuranceCompanyId);

    List<Policy> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, PolicyStatus status);

    Page<Policy> findByInsuranceCompanyId(UUID insuranceCompanyId, Pageable pageable);

    Page<Policy> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, PolicyStatus status, Pageable pageable);

    List<Policy> findByPatientProfileId(UUID patientProfileId);

    Page<Policy> findByPatientProfileId(UUID patientProfileId, Pageable pageable);

    @Query("""
            select p from Policy p
            where (:companyId is null or p.insuranceCompanyId = :companyId)
              and (:patientProfileId is null or p.patientProfileId = :patientProfileId)
              and (:status is null or p.status = :status)
            """)
    Page<Policy> search(UUID companyId, UUID patientProfileId, PolicyStatus status, Pageable pageable);

    boolean existsByPolicyNumber(String policyNumber);
}
