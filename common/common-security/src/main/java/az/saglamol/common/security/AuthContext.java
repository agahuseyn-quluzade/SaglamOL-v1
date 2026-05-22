package az.saglamol.common.security;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AuthContext(
        UUID userId,
        List<String> roles,
        String correlationId,
        Map<String, String> rawHeaders
) {

    public AuthContext {
        roles = List.copyOf(roles);
        rawHeaders = Map.copyOf(rawHeaders);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(String... requiredRoles) {
        for (String role : requiredRoles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}
