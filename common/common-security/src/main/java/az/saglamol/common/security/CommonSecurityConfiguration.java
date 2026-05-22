package az.saglamol.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@AutoConfiguration
public class CommonSecurityConfiguration {

    @Bean
    AuthContextResolver authContextResolver() {
        return new AuthContextResolver();
    }

    @Bean
    RoleChecker roleChecker() {
        return new RoleChecker();
    }

    @Bean
    @ConditionalOnProperty(prefix = "saglamol.security.internal-auth", name = "enabled", havingValue = "true")
    InternalAuthFilter internalAuthFilter(
            AuthContextResolver authContextResolver,
            @Value("${saglamol.security.internal-auth.public-path-prefixes:/actuator,/v3/api-docs,/swagger-ui}") String publicPathPrefixes
    ) {
        return new InternalAuthFilter(
                authContextResolver,
                Arrays.stream(publicPathPrefixes.split(","))
                        .map(String::trim)
                        .filter(prefix -> !prefix.isBlank())
                        .toList()
        );
    }
}
