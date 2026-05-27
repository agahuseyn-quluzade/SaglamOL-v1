package az.saglamol.claim.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimItemRequest(
        @NotBlank String description,
        String serviceCode,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotNull @Min(1) Integer quantity,
        LocalDate serviceDate,
        UUID documentId
) {
}
