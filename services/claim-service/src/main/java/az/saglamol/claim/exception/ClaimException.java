package az.saglamol.claim.exception;

import az.saglamol.common.exception.BaseBusinessException;

public class ClaimException extends BaseBusinessException {
    public ClaimException(String errorCode, String message) {
        super(errorCode, message);
    }
}
