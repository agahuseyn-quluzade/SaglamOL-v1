package az.saglamol.common.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalAuthFilterTest {

    private final InternalAuthFilter filter = new InternalAuthFilter(new AuthContextResolver(), List.of("/actuator"));

    @AfterEach
    void clearContext() {
        AuthContextHolder.clear();
        MDC.clear();
    }

    @Test
    void storesCorrelationIdInMdcAndResponseHeader() throws ServletException, IOException {
        MockHttpServletRequest request = authenticatedRequest("/api/v1/test", "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals("corr-123", MDC.get(InternalAuthFilter.MDC_CORRELATION_ID));
            assertEquals("corr-123", AuthContextHolder.getRequired().correlationId());
        });

        assertEquals("corr-123", response.getHeader(InternalAuthHeaders.CORRELATION_ID));
    }

    @Test
    void clearsContextAfterRequest() throws ServletException, IOException {
        MockHttpServletRequest request = authenticatedRequest("/api/v1/test", "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertTrue(AuthContextHolder.get().isPresent());
            assertEquals("corr-123", MDC.get(InternalAuthFilter.MDC_CORRELATION_ID));
        });

        assertFalse(AuthContextHolder.get().isPresent());
        assertEquals(null, MDC.get(InternalAuthFilter.MDC_CORRELATION_ID));
    }

    @Test
    void returnsUnauthorizedForMissingGatewayHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new AssertionError("Filter chain must not continue without gateway identity headers");
        });

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("MISSING_AUTH_HEADER"));
    }

    @Test
    void returnsForbiddenForInsufficientRole() throws ServletException, IOException {
        MockHttpServletRequest request = authenticatedRequest("/api/v1/test", "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new InternalAuthException("INSUFFICIENT_ROLE", "Access denied");
        });

        assertEquals(403, response.getStatus());
        assertEquals("corr-123", response.getHeader(InternalAuthHeaders.CORRELATION_ID));
        assertTrue(response.getContentAsString().contains("INSUFFICIENT_ROLE"));
    }

    private MockHttpServletRequest authenticatedRequest(String path, String correlationId) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.addHeader(InternalAuthHeaders.USER_ID, UUID.randomUUID().toString());
        request.addHeader(InternalAuthHeaders.USER_ROLES, RoleConstants.PATIENT);
        request.addHeader(InternalAuthHeaders.CORRELATION_ID, correlationId);
        return request;
    }
}
