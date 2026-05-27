package az.saglamol.airisk.config;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.InternalAuthHeaders;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class FeignInternalClientConfig {

    @Bean
    RequestInterceptor internalHeadersRequestInterceptor(
            @Value("${saglamol.security.internal-auth.secret:${INTERNAL_SERVICE_SECRET:dev-internal-secret}}") String internalSecret
    ) {
        return template -> {
            template.header("X-Internal-Service-Secret", internalSecret);
            AuthContextHolder.get().ifPresent(context -> template.header(
                    InternalAuthHeaders.CORRELATION_ID,
                    context.correlationId()
            ));
        };
    }

    @Bean
    Request.Options feignRequestOptions(
            @Value("${ai-risk.feign.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${ai-risk.feign.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        return new Request.Options(connectTimeoutMs, TimeUnit.MILLISECONDS, readTimeoutMs, TimeUnit.MILLISECONDS, true);
    }
}
