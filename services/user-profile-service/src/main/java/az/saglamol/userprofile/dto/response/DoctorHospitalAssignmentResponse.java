package az.saglamol.userprofile.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DoctorHospitalAssignmentResponse(
        UUID id,
        UUID doctorProfileId,
        UUID hospitalId,
        UUID branchId,
        String department,
        boolean primaryAssignment,
        Instant createdAt
) {
}
