package az.saglamol.policy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyStatusChangeRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
