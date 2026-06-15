package az.saglamol.gateway.filter;

import az.saglamol.common.security.InternalAuthHeaders;
import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.common.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
public class GatewaySecurityFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/iam/register",
            "/api/v1/iam/login",
            "/api/v1/iam/login/email",
            "/api/v1/iam/login/phone",
            "/api/v1/iam/refresh",
            "/actuator/health"
    );

    private final JwtTokenService jwtTokenService;

    public GatewaySecurityFilter(@Value("${iam.jwt.secret}") String jwtSecret) {
        this.jwtTokenService = new JwtTokenService(jwtSecret, Duration.ofMinutes(15));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = correlationId(exchange);
        String path = exchange.getRequest().getPath().value();

        if (isPublic(path)) {
            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header(InternalAuthHeaders.CORRELATION_ID, correlationId)
                            .build())
                    .build());
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            JwtPrincipal principal = jwtTokenService.parse(authorization.substring(7));
            String roleHeader = String.join(",", principal.roles());

            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header(InternalAuthHeaders.USER_ID, principal.userId().toString())
                            .header(InternalAuthHeaders.USER_ROLES, roleHeader)
                            .header(InternalAuthHeaders.CORRELATION_ID, correlationId)
                            .build())
                    .build());
        } catch (RuntimeException exception) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::equals);
    }

    private String correlationId(ServerWebExchange exchange) {
        String existing = exchange.getRequest().getHeaders().getFirst(InternalAuthHeaders.CORRELATION_ID);
        return existing == null || existing.isBlank() ? UUID.randomUUID().toString() : existing;
    }
}
