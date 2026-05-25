package az.saglamol.common.events.profile;

import java.time.Instant;
import java.util.UUID;

public record DoctorAssignedToHospitalEvent(
        UUID doctorProfileId,
        UUID hospitalId,
        UUID branchId,
        Instant occurredAt
) {
}
