package az.saglamol.policy.repository;

import az.saglamol.policy.entity.ProviderContract;
import az.saglamol.policy.entity.ProviderContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderContractRepository extends JpaRepository<ProviderContract, UUID> {
    List<ProviderContract> findByInsuranceCompanyId(UUID insuranceCompanyId);

    List<ProviderContract> findByHospitalId(UUID hospitalId);

    List<ProviderContract> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, ProviderContractStatus status);

    List<ProviderContract> findByInsuranceCompanyIdAndHospitalIdAndStatus(
            UUID insuranceCompanyId,
            UUID hospitalId,
            ProviderContractStatus status
    );

    Optional<ProviderContract> findByInsuranceCompanyIdAndContractNumber(UUID insuranceCompanyId, String contractNumber);

    boolean existsByInsuranceCompanyIdAndContractNumber(UUID insuranceCompanyId, String contractNumber);
}
