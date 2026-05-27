package az.saglamol.claim.config;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextHolder;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeignInternalClientConfigTest {

    private final FeignInternalClientConfig config = new FeignInternalClientConfig();

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void feignInterceptorAddsInternalSecretAndCorrelationId() {
        AuthContextHolder.set(new AuthContext(
                UUID.randomUUID(),
                List.of("PATIENT"),
                "corr-123",
                Map.of()
        ));
        RequestTemplate template = new RequestTemplate();

        config.internalHeadersRequestInterceptor("secret-value").apply(template);

        assertEquals(List.of("secret-value"), template.headers().get("X-Internal-Service-Secret"));
        assertEquals(List.of("corr-123"), template.headers().get("X-Correlation-Id"));
    }

    @Test
    void timeoutConfigIsApplied() {
        Request.Options options = config.feignRequestOptions(2000, 5000);

        assertEquals(2000, options.connectTimeoutMillis());
        assertEquals(5000, options.readTimeoutMillis());
    }
}
