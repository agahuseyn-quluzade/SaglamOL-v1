package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.HospitalBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HospitalBranchRepository extends JpaRepository<HospitalBranch, UUID> {

    List<HospitalBranch> findAllByHospitalId(UUID hospitalId);

    Optional<HospitalBranch> findByIdAndHospitalId(UUID id, UUID hospitalId);
}
