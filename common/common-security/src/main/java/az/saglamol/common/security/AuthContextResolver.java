package az.saglamol.common.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuthContextResolver {

    public AuthContext resolve(HttpServletRequest request) {
        String userIdHeader = requiredHeader(request, InternalAuthHeaders.USER_ID);
        String rolesHeader = requiredHeader(request, InternalAuthHeaders.USER_ROLES);
        String correlationId = correlationId(request);

        UUID userId;
        try {
            userId = UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException exception) {
            throw new InternalAuthException("INVALID_AUTH_HEADER", "X-User-Id must be a valid UUID");
        }

        List<String> roles = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .distinct()
                .toList();
        if (roles.isEmpty()) {
            throw new InternalAuthException("MISSING_AUTH_HEADER", "X-User-Roles must contain at least one role");
        }

        return new AuthContext(
                userId,
                roles,
                correlationId,
                Map.of(
                        InternalAuthHeaders.USER_ID, userIdHeader,
                        InternalAuthHeaders.USER_ROLES, rolesHeader,
                        InternalAuthHeaders.CORRELATION_ID, correlationId
                )
        );
    }

    private String requiredHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            throw new InternalAuthException("MISSING_AUTH_HEADER", headerName + " header is required");
        }
        return value;
    }

    private String correlationId(HttpServletRequest request) {
        String value = request.getHeader(InternalAuthHeaders.CORRELATION_ID);
        if (value == null || value.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return value.trim();
    }
}
