package az.saglamol.common.exception;

public class BaseBusinessException extends RuntimeException {

    private final String errorCode;

    public BaseBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
