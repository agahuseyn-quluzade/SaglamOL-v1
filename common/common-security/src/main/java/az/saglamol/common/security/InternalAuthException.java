package az.saglamol.common.security;

public class InternalAuthException extends RuntimeException {

    private final String errorCode;

    public InternalAuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
