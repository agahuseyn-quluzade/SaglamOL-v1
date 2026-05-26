package az.saglamol.payment.dto.response;

import az.saglamol.payment.entity.TransactionStatus;
import az.saglamol.payment.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentTransactionResponse(
        UUID id,
        UUID paymentId,
        TransactionType transactionType,
        BigDecimal amount,
        TransactionStatus status,
        String providerResponse,
        Instant createdAt
) {
}
