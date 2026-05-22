package az.saglamol.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthContextResolverTest {

    private final AuthContextResolver resolver = new AuthContextResolver();

    @Test
    void parsesValidGatewayHeaders() {
        UUID userId = UUID.randomUUID();
        MockHttpServletRequest request = request(userId.toString(), "PATIENT, DOCTOR, PATIENT", "corr-1");

        AuthContext context = resolver.resolve(request);

        assertEquals(userId, context.userId());
        assertEquals("corr-1", context.correlationId());
        assertEquals(2, context.roles().size());
        assertEquals("PATIENT", context.roles().getFirst());
        assertEquals("DOCTOR", context.roles().get(1));
    }

    @Test
    void rejectsMissingUserIdHeader() {
        MockHttpServletRequest request = request(null, "PATIENT", "corr-1");

        InternalAuthException exception = assertThrows(InternalAuthException.class, () -> resolver.resolve(request));

        assertEquals("MISSING_AUTH_HEADER", exception.getErrorCode());
    }

    @Test
    void rejectsMissingRolesHeader() {
        MockHttpServletRequest request = request(UUID.randomUUID().toString(), null, "corr-1");

        InternalAuthException exception = assertThrows(InternalAuthException.class, () -> resolver.resolve(request));

        assertEquals("MISSING_AUTH_HEADER", exception.getErrorCode());
    }

    @Test
    void generatesCorrelationIdWhenHeaderIsMissing() {
        MockHttpServletRequest request = request(UUID.randomUUID().toString(), "PATIENT", null);

        AuthContext context = resolver.resolve(request);

        assertFalse(context.correlationId().isBlank());
    }

    private MockHttpServletRequest request(String userId, String roles, String correlationId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (userId != null) {
            request.addHeader(InternalAuthHeaders.USER_ID, userId);
        }
        if (roles != null) {
            request.addHeader(InternalAuthHeaders.USER_ROLES, roles);
        }
        if (correlationId != null) {
            request.addHeader(InternalAuthHeaders.CORRELATION_ID, correlationId);
        }
        return request;
    }
}
