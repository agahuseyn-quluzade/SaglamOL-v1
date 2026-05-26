package az.saglamol.payment.dto.response;

import az.saglamol.payment.entity.PaymentProvider;
import az.saglamol.payment.entity.PaymentStatus;
import az.saglamol.payment.entity.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String paymentNumber,
        UUID policyId,
        UUID insuranceCompanyId,
        UUID patientProfileId,
        UUID claimId,
        UUID hospitalId,
        BigDecimal amount,
        String currency,
        PaymentType paymentType,
        PaymentStatus status,
        PaymentProvider provider,
        String providerReference,
        Instant createdAt,
        Instant updatedAt
) {
}
