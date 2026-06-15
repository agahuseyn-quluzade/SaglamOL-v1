package az.saglamol.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleConstantsTest {

    @Test
    void exposesSupportedRoles() {
        assertEquals("PATIENT", RoleConstants.PATIENT);
        assertEquals("DOCTOR", RoleConstants.DOCTOR);
        assertEquals("AGENT", RoleConstants.AGENT);
        assertEquals("ADMIN", RoleConstants.ADMIN);
        assertEquals("SYSTEM", RoleConstants.SYSTEM);
    }
}
