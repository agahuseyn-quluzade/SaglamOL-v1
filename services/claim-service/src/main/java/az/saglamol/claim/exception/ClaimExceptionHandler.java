package az.saglamol.claim.exception;

import az.saglamol.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ClaimExceptionHandler {

    @ExceptionHandler(ClaimException.class)
    ResponseEntity<ErrorResponse> handleClaimException(ClaimException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "CLAIM_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CLAIM_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "INTERNAL_CLIENT_NOT_CONFIGURED" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(error(status, exception.getErrorCode(), exception.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(error(status, "VALIDATION_FAILED", "Request validation failed", request));
    }

    private ErrorResponse error(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                request.getHeader("X-Correlation-Id")
        );
    }
}
