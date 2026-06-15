package az.saglamol.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseBusinessExceptionTest {

    @Test
    void keepsErrorCodeAndMessage() {
        BaseBusinessException exception = new BaseBusinessException("TEST_ERROR", "Test message");

        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertEquals("Test message", exception.getMessage());
    }
}
