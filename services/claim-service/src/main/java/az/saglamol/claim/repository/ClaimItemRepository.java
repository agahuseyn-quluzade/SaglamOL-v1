package az.saglamol.claim.repository;

import az.saglamol.claim.entity.ClaimItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimItemRepository extends JpaRepository<ClaimItem, UUID> {
    List<ClaimItem> findByClaimId(UUID claimId);
}
