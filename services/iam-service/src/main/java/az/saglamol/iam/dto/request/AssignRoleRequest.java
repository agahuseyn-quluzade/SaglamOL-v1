package az.saglamol.iam.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleRequest(
        @NotBlank String roleName
) {
}
