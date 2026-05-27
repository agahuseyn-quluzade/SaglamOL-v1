package az.saglamol.healthrecord.repository;

import az.saglamol.healthrecord.entity.DocumentHashIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentHashIndexRepository extends JpaRepository<DocumentHashIndex, UUID> {
    Optional<DocumentHashIndex> findBySha256Hash(String sha256Hash);

    boolean existsBySha256Hash(String sha256Hash);

    List<DocumentHashIndex> findByPatientProfileId(UUID patientProfileId);

    List<DocumentHashIndex> findByClaimId(UUID claimId);

    List<DocumentHashIndex> findByHospitalId(UUID hospitalId);
}
