package az.saglamol.claim.dto.request;

import az.saglamol.claim.entity.ClaimStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeClaimStatusRequest(
        @NotNull ClaimStatus toStatus,
        UUID changedByUserId,
        String reason
) {
}
