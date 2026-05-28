package az.saglamol.policy.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;
import java.util.UUID;

public record IssuePolicyRequest(
        @NotNull UUID productId,
        @NotNull UUID patientProfileId,
        UUID agentProfileId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
    @AssertTrue(message = "endDate must be on or after startDate")
    public boolean isValidDateRange() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
