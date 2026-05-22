package az.saglamol.userprofile.exception;

import az.saglamol.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class UserProfileExceptionHandler {

    @ExceptionHandler(UserProfileException.class)
    ResponseEntity<ErrorResponse> handleUserProfileException(UserProfileException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "HOSPITAL_NOT_FOUND", "DOCTOR_NOT_FOUND", "BRANCH_NOT_FOUND",
                 "PATIENT_PROFILE_NOT_FOUND", "DOCTOR_PROFILE_NOT_FOUND", "AGENT_PROFILE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "HOSPITAL_ALREADY_EXISTS", "STAFF_ALREADY_EXISTS", "DOCTOR_ALREADY_ASSIGNED",
                 "PROFILE_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
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
