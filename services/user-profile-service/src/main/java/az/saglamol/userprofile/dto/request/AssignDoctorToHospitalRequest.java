package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AssignDoctorToHospitalRequest(
        UUID branchId,
        @Size(max = 120) String department,
        boolean primaryAssignment
) {
}
