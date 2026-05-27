package az.saglamol.healthrecord.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TreatmentResponse(
        UUID id,
        UUID healthRecordId,
        String serviceType,
        String treatmentType,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String medications,
        BigDecimal estimatedCost,
        BigDecimal actualCost,
        Instant createdAt,
        Instant updatedAt
) {
}
