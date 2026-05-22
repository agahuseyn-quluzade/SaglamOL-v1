package az.saglamol.iam.exception;

import az.saglamol.common.exception.BaseBusinessException;

public class IamException extends BaseBusinessException {

    public IamException(String errorCode, String message) {
        super(errorCode, message);
    }
}
