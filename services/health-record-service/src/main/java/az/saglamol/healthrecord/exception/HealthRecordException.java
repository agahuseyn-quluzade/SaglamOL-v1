package az.saglamol.healthrecord.exception;

import az.saglamol.common.exception.BaseBusinessException;

public class HealthRecordException extends BaseBusinessException {
    public HealthRecordException(String errorCode, String message) {
        super(errorCode, message);
    }
}
