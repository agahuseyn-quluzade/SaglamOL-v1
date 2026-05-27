package az.saglamol.claim.repository;

import az.saglamol.claim.entity.ClaimDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimDecisionRepository extends JpaRepository<ClaimDecision, UUID> {
    List<ClaimDecision> findByClaimId(UUID claimId);
}
