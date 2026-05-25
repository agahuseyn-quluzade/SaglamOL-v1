package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.InsuranceCompanyStaffProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsuranceCompanyStaffProfileRepository extends JpaRepository<InsuranceCompanyStaffProfile, UUID> {

    Optional<InsuranceCompanyStaffProfile> findByIamUserId(UUID iamUserId);

    Optional<InsuranceCompanyStaffProfile> findByIamUserIdAndInsuranceCompanyId(UUID iamUserId, UUID insuranceCompanyId);

    List<InsuranceCompanyStaffProfile> findAllByInsuranceCompanyId(UUID insuranceCompanyId);

    Page<InsuranceCompanyStaffProfile> findAllByInsuranceCompanyId(UUID insuranceCompanyId, Pageable pageable);

    boolean existsByIamUserIdAndInsuranceCompanyId(UUID iamUserId, UUID insuranceCompanyId);

    boolean existsByInsuranceCompanyIdAndEmployeeCode(UUID insuranceCompanyId, String employeeCode);
}
