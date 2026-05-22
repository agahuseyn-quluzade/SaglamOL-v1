package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateHospitalStaffRequest(
        @NotNull UUID userId,
        UUID branchId,
        @NotBlank @Size(max = 120) String position
) {
}
