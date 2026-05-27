package az.saglamol.claim.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimItemResponse(
        UUID id,
        UUID claimId,
        String description,
        String serviceCode,
        BigDecimal amount,
        Integer quantity,
        LocalDate serviceDate,
        UUID documentId,
        Instant createdAt,
        Instant updatedAt
) {
}
