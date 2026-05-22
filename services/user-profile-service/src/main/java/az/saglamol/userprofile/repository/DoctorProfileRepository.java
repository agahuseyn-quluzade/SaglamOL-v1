package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {

    Optional<DoctorProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByLicenseNo(String licenseNo);

    boolean existsByLicenseNoAndUserIdNot(String licenseNo, UUID userId);
}
