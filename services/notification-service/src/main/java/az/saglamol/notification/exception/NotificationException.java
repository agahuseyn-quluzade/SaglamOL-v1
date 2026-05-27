package az.saglamol.notification.exception;

public class NotificationException extends RuntimeException {
    private final String errorCode;

    public NotificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
