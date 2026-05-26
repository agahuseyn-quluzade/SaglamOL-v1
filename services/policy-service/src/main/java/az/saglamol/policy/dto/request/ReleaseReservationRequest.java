package az.saglamol.policy.dto.request;

import jakarta.validation.constraints.Size;

public record ReleaseReservationRequest(
        @Size(max = 500) String reason
) {
}
