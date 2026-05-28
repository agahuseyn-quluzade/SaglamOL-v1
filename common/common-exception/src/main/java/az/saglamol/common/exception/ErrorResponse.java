package az.saglamol.common.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        String correlationId,
        Instant timestamp,
        Map<String, Object> details
) {
    public ErrorResponse(
            Instant timestamp,
            int status,
            String errorCode,
            String message,
            String path,
            String correlationId
    ) {
        this(
                errorCode,
                message,
                correlationId,
                timestamp,
                Map.of("status", status, "path", path)
        );
    }
}
