package az.saglamol.claim.repository;

import az.saglamol.claim.entity.ClaimDocumentReference;
import az.saglamol.claim.entity.ClaimDocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimDocumentReferenceRepository extends JpaRepository<ClaimDocumentReference, UUID> {
    List<ClaimDocumentReference> findByClaimId(UUID claimId);

    List<ClaimDocumentReference> findByClaimIdAndStatus(UUID claimId, ClaimDocumentStatus status);
}
