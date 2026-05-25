package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record InsuranceCompanyStaffCreatedEvent(
        UUID companyId,
        UUID staffProfileId,
        UUID iamUserId,
        String roleType,
        Instant occurredAt
) {
}
