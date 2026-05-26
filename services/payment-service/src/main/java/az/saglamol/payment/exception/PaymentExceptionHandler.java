package az.saglamol.payment.exception;

import az.saglamol.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    ResponseEntity<ErrorResponse> handlePaymentException(PaymentException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case "PAYMENT_NOT_FOUND", "INVOICE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "PAYMENT_ALREADY_COMPLETED", "PAYMENT_NOT_COMPLETED", "INVALID_PAYMENT_STATUS",
                 "INVOICE_ALREADY_PAID", "INVALID_INVOICE_STATUS" -> HttpStatus.CONFLICT;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(error(status, exception.getErrorCode(), exception.getMessage(), request));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return ResponseEntity.status(status).body(error(status, "REQUEST_FAILED", exception.getReason(), request));
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
