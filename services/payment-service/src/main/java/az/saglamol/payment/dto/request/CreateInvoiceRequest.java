package az.saglamol.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInvoiceRequest(
        UUID policyId,
        UUID claimId,
        @NotNull UUID insuranceCompanyId,
        UUID hospitalId,
        UUID patientProfileId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String currency
) {
}
