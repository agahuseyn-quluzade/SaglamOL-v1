package az.saglamol.policy.exception;

import az.saglamol.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class PolicyExceptionHandler {

    @ExceptionHandler(PolicyException.class)
    ResponseEntity<ErrorResponse> handlePolicyException(PolicyException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "PRODUCT_NOT_FOUND", "COVERAGE_RULE_NOT_FOUND", "PROVIDER_CONTRACT_NOT_FOUND",
                 "POLICY_NOT_FOUND", "PATIENT_PROFILE_NOT_FOUND", "LIMIT_RESERVATION_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "PRODUCT_ALREADY_EXISTS", "COVERAGE_RULE_ALREADY_EXISTS", "PROVIDER_CONTRACT_ALREADY_EXISTS",
                 "LIMIT_ALREADY_RESERVED" -> HttpStatus.CONFLICT;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
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
