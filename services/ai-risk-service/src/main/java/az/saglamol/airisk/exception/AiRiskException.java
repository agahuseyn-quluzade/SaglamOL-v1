package az.saglamol.airisk.exception;

public class AiRiskException extends RuntimeException {
    private final String errorCode;

    public AiRiskException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
