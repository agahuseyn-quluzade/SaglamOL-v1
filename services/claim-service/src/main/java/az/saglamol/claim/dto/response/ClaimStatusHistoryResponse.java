package az.saglamol.claim.dto.response;

import az.saglamol.claim.entity.ClaimStatus;

import java.time.Instant;
import java.util.UUID;

public record ClaimStatusHistoryResponse(
        UUID id,
        UUID claimId,
        ClaimStatus fromStatus,
        ClaimStatus toStatus,
        UUID changedByUserId,
        String reason,
        Instant createdAt
) {
}
