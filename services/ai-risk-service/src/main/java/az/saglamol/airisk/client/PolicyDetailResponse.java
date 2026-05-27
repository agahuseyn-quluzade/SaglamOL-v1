package az.saglamol.airisk.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyDetailResponse(
        UUID id,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal annualLimit,
        BigDecimal usedLimit,
        BigDecimal reservedLimit
) {
}
