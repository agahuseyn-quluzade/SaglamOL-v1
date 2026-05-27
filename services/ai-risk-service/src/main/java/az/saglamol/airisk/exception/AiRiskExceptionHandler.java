package az.saglamol.airisk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class AiRiskExceptionHandler {

    @ExceptionHandler(AiRiskException.class)
    ResponseEntity<Map<String, Object>> handle(AiRiskException exception) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "FORBIDDEN", "PATIENT_FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "AI_RISK_ASSESSMENT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "errorCode", exception.getErrorCode(),
                "message", exception.getMessage()
        ));
    }
}
