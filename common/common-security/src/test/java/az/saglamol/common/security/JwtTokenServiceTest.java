package az.saglamol.common.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {

    @Test
    void generatesAndParsesAccessToken() {
        JwtTokenService tokenService = new JwtTokenService(
                "test-jwt-secret-contains-at-least-32-characters",
                Duration.ofMinutes(15)
        );
        UUID userId = UUID.randomUUID();
        JwtPrincipal principal = new JwtPrincipal(
                userId,
                "user@gmail.com",
                "+994501234567",
                List.of(RoleConstants.PATIENT)
        );

        JwtPrincipal parsed = tokenService.parse(tokenService.generateAccessToken(principal));

        assertEquals(userId, parsed.userId());
        assertEquals("user@gmail.com", parsed.email());
        assertEquals("+994501234567", parsed.phoneNumber());
        assertEquals(List.of(RoleConstants.PATIENT), parsed.roles());
    }

    @Test
    void rejectsBlankOrWeakSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenService("", Duration.ofMinutes(15)));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenService("short-secret", Duration.ofMinutes(15)));
    }
}
