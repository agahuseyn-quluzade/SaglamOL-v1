package az.saglamol.policy.repository;

import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, UUID> {
    List<InsuranceProduct> findByInsuranceCompanyId(UUID insuranceCompanyId);

    Page<InsuranceProduct> findByInsuranceCompanyId(UUID insuranceCompanyId, Pageable pageable);

    List<InsuranceProduct> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, InsuranceProductStatus status);

    Page<InsuranceProduct> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, InsuranceProductStatus status, Pageable pageable);

    List<InsuranceProduct> findByStatus(InsuranceProductStatus status);

    Page<InsuranceProduct> findByStatus(InsuranceProductStatus status, Pageable pageable);

    Optional<InsuranceProduct> findByInsuranceCompanyIdAndProductCode(UUID insuranceCompanyId, String productCode);

    boolean existsByInsuranceCompanyIdAndProductCode(UUID insuranceCompanyId, String productCode);
}
