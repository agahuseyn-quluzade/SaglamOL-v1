package az.saglamol.fraud.exception;

import az.saglamol.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class FraudExceptionHandler {

    @ExceptionHandler(FraudException.class)
    ResponseEntity<ErrorResponse> handle(FraudException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "FRAUD_ASSESSMENT_NOT_FOUND", "CLAIM_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "PATIENT_FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "INTERNAL_CLIENT_NOT_CONFIGURED" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                exception.getErrorCode(),
                exception.getMessage(),
                request.getRequestURI(),
                request.getHeader("X-Correlation-Id")
        ));
    }
}
