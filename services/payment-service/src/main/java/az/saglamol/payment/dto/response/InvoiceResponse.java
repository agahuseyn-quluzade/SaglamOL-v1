package az.saglamol.payment.dto.response;

import az.saglamol.payment.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        UUID policyId,
        UUID claimId,
        UUID insuranceCompanyId,
        UUID hospitalId,
        UUID patientProfileId,
        BigDecimal amount,
        String currency,
        InvoiceStatus status,
        Instant issuedAt,
        Instant paidAt,
        Instant createdAt,
        Instant updatedAt
) {
}
