package az.saglamol.healthrecord.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DocumentHashBatchRequest(
        @NotEmpty List<@NotNull UUID> documentIds
) {
}
