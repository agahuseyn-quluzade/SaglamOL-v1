package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.InsuranceCompany;
import az.saglamol.userprofile.entity.InsuranceCompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, UUID> {

    Optional<InsuranceCompany> findByTaxId(String taxId);

    Optional<InsuranceCompany> findByLicenseNumber(String licenseNumber);

    List<InsuranceCompany> findByStatus(InsuranceCompanyStatus status);

    Page<InsuranceCompany> findByStatus(InsuranceCompanyStatus status, Pageable pageable);

    boolean existsByTaxId(String taxId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByIdAndStatus(UUID id, InsuranceCompanyStatus status);
}
