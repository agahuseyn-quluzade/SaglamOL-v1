package az.saglamol.policy.repository;

import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByInsuranceCompanyId(UUID insuranceCompanyId);

    List<Policy> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, PolicyStatus status);

    List<Policy> findByPatientProfileId(UUID patientProfileId);

    boolean existsByPolicyNumber(String policyNumber);
}
