package az.saglamol.claim.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ReviewClaimRequest(
        @DecimalMin("0.00") BigDecimal approvedAmount,
        String reason
) {
}
