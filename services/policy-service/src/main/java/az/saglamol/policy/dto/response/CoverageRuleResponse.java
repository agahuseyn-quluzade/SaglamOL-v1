package az.saglamol.policy.dto.response;

import az.saglamol.policy.entity.RuleStatus;
import az.saglamol.policy.entity.ServiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CoverageRuleResponse(
        UUID id,
        UUID productId,
        ServiceType serviceType,
        Integer coveragePercent,
        BigDecimal maxAmount,
        Integer waitingPeriodDays,
        boolean requiresPreApproval,
        RuleStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
