package az.saglamol.policy.dto.request;

import az.saglamol.policy.entity.PayoutModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateProviderContractRequest(
        @NotNull UUID insuranceCompanyId,
        @NotNull UUID hospitalId,
        UUID productId,
        @NotBlank @Size(max = 80) String contractNumber,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull PayoutModel payoutModel
) {
}
