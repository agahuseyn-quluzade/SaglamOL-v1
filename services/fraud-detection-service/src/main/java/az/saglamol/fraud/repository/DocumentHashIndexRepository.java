package az.saglamol.fraud.repository;

import az.saglamol.fraud.entity.DocumentHashIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentHashIndexRepository extends JpaRepository<DocumentHashIndex, UUID> {
    List<DocumentHashIndex> findBySha256Hash(String sha256Hash);

    List<DocumentHashIndex> findByClaimId(UUID claimId);

    List<DocumentHashIndex> findByPatientProfileId(UUID patientProfileId);

    boolean existsBySha256HashAndClaimIdNot(String sha256Hash, UUID claimId);
}
