package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HospitalRepository extends JpaRepository<Hospital, UUID> {

    boolean existsByTaxId(String taxId);

    boolean existsByLicenseNo(String licenseNo);
}
