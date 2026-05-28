package az.saglamol.fraud.client;

import java.util.List;
import java.util.UUID;

public record DocumentHashBatchRequest(
        List<UUID> documentIds
) {
}
