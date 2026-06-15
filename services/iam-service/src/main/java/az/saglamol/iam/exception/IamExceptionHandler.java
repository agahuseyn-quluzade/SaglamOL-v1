package az.saglamol.iam.exception;

import az.saglamol.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class IamExceptionHandler {

    @ExceptionHandler(IamException.class)
    ResponseEntity<ErrorResponse> handleIamException(IamException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "INVALID_CREDENTIALS", "INVALID_REFRESH_TOKEN", "REFRESH_TOKEN_REUSED" -> HttpStatus.UNAUTHORIZED;
            case "EMAIL_ALREADY_EXISTS", "PHONE_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "USER_NOT_FOUND", "ROLE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(error(status, exception.getErrorCode(), exception.getMessage(), request));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(error(status, "UNAUTHORIZED", "Authentication is required", request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(error(status, "FORBIDDEN", "Access is denied", request));
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
