package az.saglamol.iam.security;

import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.common.security.JwtTokenService;
import az.saglamol.iam.entity.Role;
import az.saglamol.iam.entity.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final JwtTokenService jwtTokenService;

    public JwtTokenProvider(
            @Value("${iam.jwt.secret}") String secret,
            @Value("${iam.jwt.access-token-minutes}") long accessTokenMinutes
    ) {
        this.jwtTokenService = new JwtTokenService(secret, Duration.ofMinutes(accessTokenMinutes));
    }

    public String generateAccessToken(UserAccount user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .toList();

        return jwtTokenService.generateAccessToken(
                new JwtPrincipal(user.getId(), user.getEmail(), user.getPhoneNumber(), roles)
        );
    }

    public JwtPrincipal parse(String token) {
        return jwtTokenService.parse(token);
    }

    public long accessTokenTtlSeconds() {
        return jwtTokenService.accessTokenTtlSeconds();
    }
}
