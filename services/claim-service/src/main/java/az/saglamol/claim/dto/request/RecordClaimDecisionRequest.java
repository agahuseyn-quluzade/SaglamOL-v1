package az.saglamol.claim.dto.request;

import az.saglamol.claim.entity.ClaimDecisionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordClaimDecisionRequest(
        @NotNull ClaimDecisionType decision,
        @NotNull UUID decidedBy,
        @NotBlank String reason,
        @DecimalMin("0.00") BigDecimal approvedAmount
) {
}
