package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkAgentToCompanyRequest(
        @NotNull UUID insuranceCompanyId
) {
}
