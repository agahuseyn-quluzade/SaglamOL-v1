package az.saglamol.iam.dto.request;

import az.saglamol.iam.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull UserStatus status
) {
}
