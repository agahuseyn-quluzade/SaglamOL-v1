package az.saglamol.fraud.exception;

import az.saglamol.common.exception.BaseBusinessException;

public class FraudException extends BaseBusinessException {
    public FraudException(String errorCode, String message) {
        super(errorCode, message);
    }
}
