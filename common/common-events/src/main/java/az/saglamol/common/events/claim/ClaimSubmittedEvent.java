package az.saglamol.common.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClaimSubmittedEvent(
        UUID claimId,
        String claimNumber,
        UUID policyId,
        UUID companyId,
        UUID patientProfileId,
        UUID hospitalId,
        UUID doctorProfileId,
        String serviceType,
        BigDecimal totalAmount,
        List<UUID> documentIds,
        Instant occurredAt
) {
}
