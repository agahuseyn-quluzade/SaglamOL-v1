package az.saglamol.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class JwtTokenService {

    private final SecretKey secretKey;
    private final Duration accessTokenTtl;

    public JwtTokenService(String secret, Duration accessTokenTtl) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
    }

    public String generateAccessToken(JwtPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.userId().toString())
                .claim("email", principal.email())
                .claim("phoneNumber", principal.phoneNumber())
                .claim("roles", principal.roles().stream().sorted().toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(secretKey)
                .compact();
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtPrincipal(
                UUID.fromString(claims.getSubject()),
                claims.get("email", String.class),
                claims.get("phoneNumber", String.class),
                rolesFromClaim(claims.get("roles"))
        );
    }

    public long accessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }

    private List<String> rolesFromClaim(Object rolesClaim) {
        if (rolesClaim instanceof List<?> roles) {
            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }
}
