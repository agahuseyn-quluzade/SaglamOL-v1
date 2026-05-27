package az.saglamol.healthrecord.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTreatmentRequest(
        @NotBlank String serviceType,
        @NotBlank String treatmentType,
        String description,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        String medications,
        @DecimalMin("0.00") BigDecimal estimatedCost,
        @DecimalMin("0.00") BigDecimal actualCost
) {
}
