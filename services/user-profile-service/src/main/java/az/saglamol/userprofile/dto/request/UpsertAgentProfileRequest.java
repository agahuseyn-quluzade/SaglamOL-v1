package az.saglamol.userprofile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertAgentProfileRequest(
        @NotBlank @Size(max = 120) String employeeNo,
        @Size(max = 160) String department
) {
}
