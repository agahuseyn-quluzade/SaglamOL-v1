package az.saglamol.iam.integration;

import az.saglamol.iam.dto.request.LoginRequest;
import az.saglamol.iam.dto.request.LogoutRequest;
import az.saglamol.iam.dto.request.RefreshTokenRequest;
import az.saglamol.iam.dto.request.RegisterRequest;
import az.saglamol.iam.dto.response.MeResponse;
import az.saglamol.iam.dto.response.RegisterResponse;
import az.saglamol.iam.dto.response.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "outbox.scheduler.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class IamAuthFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("iam_it")
            .withUsername("saglamol")
            .withPassword("saglamol");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void registerLoginMeRefreshLogoutFlow() {
        String email = "it-%s@saglamol.az".formatted(System.nanoTime());
        var register = restTemplate.postForEntity(url("/api/v1/iam/register"),
                new RegisterRequest(email, "+994501234567", "Password123!"), RegisterResponse.class);

        assertEquals(HttpStatus.CREATED, register.getStatusCode());
        assertNotNull(register.getBody());
        assertNotNull(register.getBody().userId());

        var login = restTemplate.postForEntity(url("/api/v1/iam/login"),
                new LoginRequest(email, "Password123!"), TokenResponse.class);

        assertEquals(HttpStatus.OK, login.getStatusCode());
        TokenResponse tokens = login.getBody();
        assertNotNull(tokens);
        assertFalse(tokens.accessToken().isBlank());
        assertFalse(tokens.refreshToken().isBlank());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());
        var me = restTemplate.exchange(url("/api/v1/iam/me"), HttpMethod.GET,
                new HttpEntity<>(headers), MeResponse.class);

        assertEquals(HttpStatus.OK, me.getStatusCode());
        assertEquals(register.getBody().userId(), me.getBody().userId());

        var refresh = restTemplate.postForEntity(url("/api/v1/iam/refresh"),
                new RefreshTokenRequest(tokens.refreshToken()), TokenResponse.class);

        assertEquals(HttpStatus.OK, refresh.getStatusCode());
        assertNotNull(refresh.getBody());
        assertFalse(refresh.getBody().accessToken().isBlank());

        var logout = restTemplate.postForEntity(url("/api/v1/iam/logout"),
                new LogoutRequest(refresh.getBody().refreshToken()), String.class);

        assertEquals(HttpStatus.OK, logout.getStatusCode());
    }

    private String url(String path) {
        return "http://localhost:%d%s".formatted(port, path);
    }
}
