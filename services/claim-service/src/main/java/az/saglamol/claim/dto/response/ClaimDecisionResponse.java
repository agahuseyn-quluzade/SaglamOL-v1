package az.saglamol.claim.dto.response;

import az.saglamol.claim.entity.ClaimDecisionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimDecisionResponse(
        UUID id,
        UUID claimId,
        ClaimDecisionType decision,
        UUID decidedBy,
        String reason,
        BigDecimal approvedAmount,
        Instant createdAt
) {
}
