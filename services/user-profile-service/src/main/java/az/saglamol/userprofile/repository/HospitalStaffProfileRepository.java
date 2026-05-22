package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.HospitalStaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HospitalStaffProfileRepository extends JpaRepository<HospitalStaffProfile, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<HospitalStaffProfile> findByUserId(UUID userId);

    List<HospitalStaffProfile> findAllByHospitalId(UUID hospitalId);

    List<HospitalStaffProfile> findAllByHospitalIdAndBranchId(UUID hospitalId, UUID branchId);
}
