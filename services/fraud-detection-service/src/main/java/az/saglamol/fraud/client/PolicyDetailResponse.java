package az.saglamol.fraud.client;

import java.time.LocalDate;
import java.util.UUID;

public record PolicyDetailResponse(
        UUID id,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {
}
