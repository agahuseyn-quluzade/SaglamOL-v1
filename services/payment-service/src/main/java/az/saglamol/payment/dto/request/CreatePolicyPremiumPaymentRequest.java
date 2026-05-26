package az.saglamol.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePolicyPremiumPaymentRequest(
        @NotNull UUID policyId,
        @NotNull UUID insuranceCompanyId,
        @NotNull UUID patientProfileId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String currency
) {
}
