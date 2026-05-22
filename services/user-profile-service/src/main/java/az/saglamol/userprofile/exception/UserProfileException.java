package az.saglamol.userprofile.exception;

import az.saglamol.common.exception.BaseBusinessException;

public class UserProfileException extends BaseBusinessException {

    public UserProfileException(String errorCode, String message) {
        super(errorCode, message);
    }
}
