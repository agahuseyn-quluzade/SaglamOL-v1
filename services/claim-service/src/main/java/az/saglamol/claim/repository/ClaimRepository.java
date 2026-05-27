package az.saglamol.claim.repository;

import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    Optional<Claim> findByClaimNumber(String claimNumber);

    boolean existsByClaimNumber(String claimNumber);

    List<Claim> findByPolicyId(UUID policyId);

    List<Claim> findByInsuranceCompanyId(UUID insuranceCompanyId);

    Page<Claim> findByInsuranceCompanyId(UUID insuranceCompanyId, Pageable pageable);

    List<Claim> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, ClaimStatus status);

    Page<Claim> findByInsuranceCompanyIdAndStatus(UUID insuranceCompanyId, ClaimStatus status, Pageable pageable);

    List<Claim> findByPatientProfileId(UUID patientProfileId);

    Page<Claim> findByPatientProfileId(UUID patientProfileId, Pageable pageable);

    List<Claim> findByHospitalId(UUID hospitalId);

    Page<Claim> findByHospitalId(UUID hospitalId, Pageable pageable);

    List<Claim> findByInsuranceCompanyIdAndPatientProfileId(UUID insuranceCompanyId, UUID patientProfileId);

    List<Claim> findByInsuranceCompanyIdAndHospitalId(UUID insuranceCompanyId, UUID hospitalId);

    @Query("""
            select c from Claim c
            where (:companyId is null or c.insuranceCompanyId = :companyId)
              and (:patientProfileId is null or c.patientProfileId = :patientProfileId)
              and (:hospitalId is null or c.hospitalId = :hospitalId)
              and (:status is null or c.status = :status)
              and (:fromDate is null or c.treatmentDate >= :fromDate)
              and (:toDate is null or c.treatmentDate <= :toDate)
            """)
    Page<Claim> search(UUID companyId, UUID patientProfileId, UUID hospitalId, ClaimStatus status,
                       LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
