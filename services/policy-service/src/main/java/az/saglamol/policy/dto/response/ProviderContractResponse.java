package az.saglamol.policy.dto.response;

import az.saglamol.policy.entity.PayoutModel;
import az.saglamol.policy.entity.ProviderContractStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProviderContractResponse(
        UUID id,
        UUID insuranceCompanyId,
        UUID hospitalId,
        UUID productId,
        String contractNumber,
        LocalDate startDate,
        LocalDate endDate,
        ProviderContractStatus status,
        PayoutModel payoutModel,
        Instant createdAt,
        Instant updatedAt
) {
}
