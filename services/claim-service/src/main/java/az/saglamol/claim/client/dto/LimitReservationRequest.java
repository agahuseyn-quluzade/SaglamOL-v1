package az.saglamol.claim.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LimitReservationRequest(
        UUID claimId,
        UUID companyId,
        BigDecimal amount
) {
}
