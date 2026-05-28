package az.saglamol.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimitFilter implements GlobalFilter, Ordered {

    private static final long WINDOW_MILLIS = 60_000;

    private final Map<String, ClientWindow> windows = new ConcurrentHashMap<>();
    private final Clock clock;
    private final boolean enabled;
    private final int requestsPerMinute;

    public InMemoryRateLimitFilter(
            @Value("${saglamol.gateway.rate-limit.enabled:true}") boolean enabled,
            @Value("${saglamol.gateway.rate-limit.requests-per-minute:120}") int requestsPerMinute
    ) {
        this(enabled, requestsPerMinute, Clock.systemUTC());
    }

    InMemoryRateLimitFilter(boolean enabled, int requestsPerMinute, Clock clock) {
        this.enabled = enabled;
        this.requestsPerMinute = requestsPerMinute;
        this.clock = clock;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled || requestsPerMinute <= 0) {
            return chain.filter(exchange);
        }

        String clientKey = clientKey(exchange);
        long now = clock.millis();
        ClientWindow window = windows.compute(clientKey, (key, existing) -> {
            if (existing == null || now - existing.windowStartedAt >= WINDOW_MILLIS) {
                return new ClientWindow(now, new AtomicInteger(1));
            }
            existing.counter.incrementAndGet();
            return existing;
        });

        if (window.counter.get() > requestsPerMinute) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private String clientKey(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress == null ? "unknown" : remoteAddress.getAddress().getHostAddress();
    }

    private record ClientWindow(long windowStartedAt, AtomicInteger counter) {
    }
}
