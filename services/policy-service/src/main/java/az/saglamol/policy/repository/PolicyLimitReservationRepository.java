package az.saglamol.policy.repository;

import az.saglamol.policy.entity.PolicyLimitReservation;
import az.saglamol.policy.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PolicyLimitReservationRepository extends JpaRepository<PolicyLimitReservation, UUID> {
    List<PolicyLimitReservation> findByPolicyId(UUID policyId);

    List<PolicyLimitReservation> findByClaimId(UUID claimId);

    List<PolicyLimitReservation> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, ReservationStatus status);
}
