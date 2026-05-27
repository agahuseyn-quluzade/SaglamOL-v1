package az.saglamol.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(NotificationException.class)
    ResponseEntity<Map<String, Object>> handle(NotificationException exception) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "NOTIFICATION_NOT_FOUND", "TEMPLATE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "errorCode", exception.getErrorCode(),
                "message", exception.getMessage()
        ));
    }
}
