package az.saglamol.claim.dto.request;

import az.saglamol.claim.entity.ClaimStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ClaimSearchFilters(
        UUID companyId,
        UUID patientProfileId,
        UUID hospitalId,
        ClaimStatus status,
        LocalDate fromDate,
        LocalDate toDate
) {
}
