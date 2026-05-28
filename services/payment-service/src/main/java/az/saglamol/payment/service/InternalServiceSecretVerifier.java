package az.saglamol.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class InternalServiceSecretVerifier {

    public static final String HEADER_NAME = "X-Internal-Service-Secret";

    private final String expectedSecret;

    public InternalServiceSecretVerifier(
            @Value("${saglamol.security.internal-auth.secret}") String expectedSecret
    ) {
        this.expectedSecret = expectedSecret;
    }

    public void verify(String providedSecret) {
        if (providedSecret == null || providedSecret.isBlank()
                || !MessageDigest.isEqual(bytes(expectedSecret), bytes(providedSecret))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal service secret");
        }
    }

    private byte[] bytes(String value) {
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
    }
}
