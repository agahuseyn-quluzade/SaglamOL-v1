package az.saglamol.policy.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record IssuePolicyRequest(
        @NotNull UUID productId,
        @NotNull UUID patientProfileId,
        UUID agentProfileId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
