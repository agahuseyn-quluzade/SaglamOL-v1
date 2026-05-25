package az.saglamol.policy.exception;

import az.saglamol.common.exception.BaseBusinessException;

public class PolicyException extends BaseBusinessException {

    public PolicyException(String errorCode, String message) {
        super(errorCode, message);
    }
}
