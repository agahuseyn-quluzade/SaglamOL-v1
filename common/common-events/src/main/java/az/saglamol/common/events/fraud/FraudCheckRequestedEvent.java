package az.saglamol.common.events.fraud;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FraudCheckRequestedEvent(
        UUID claimId,
        UUID companyId,
        UUID patientProfileId,
        UUID policyId,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}
