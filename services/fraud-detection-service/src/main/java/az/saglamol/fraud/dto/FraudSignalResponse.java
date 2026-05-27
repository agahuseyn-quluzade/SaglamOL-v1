package az.saglamol.fraud.dto;

import az.saglamol.fraud.entity.FraudSignalSeverity;
import az.saglamol.fraud.entity.FraudSignalType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FraudSignalResponse(
        UUID id,
        UUID fraudAssessmentId,
        FraudSignalType signalType,
        FraudSignalSeverity severity,
        BigDecimal scoreImpact,
        String message,
        Instant createdAt
) {
}
