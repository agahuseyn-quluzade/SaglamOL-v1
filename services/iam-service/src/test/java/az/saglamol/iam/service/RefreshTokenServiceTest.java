package az.saglamol.iam.service;

import az.saglamol.iam.entity.RefreshToken;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.entity.UserStatus;
import az.saglamol.iam.exception.IamException;
import az.saglamol.iam.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    @Test
    void refreshTokenReuseRevokesTokenFamily() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshTokenService service = new RefreshTokenService(repository, 30);
        UserAccount user = new UserAccount(UUID.randomUUID(), "user@gmail.com", null, "hash", UserStatus.ACTIVE, Instant.now());
        UUID familyId = UUID.randomUUID();
        RefreshToken reused = token(user, familyId);
        reused.revoke(Instant.now(), null);
        RefreshToken sibling = token(user, familyId);

        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(reused));
        when(repository.findAllByFamilyId(familyId)).thenReturn(List.of(reused, sibling));

        IamException exception = assertThrows(IamException.class, () -> service.rotate("stolen-token", "127.0.0.1", "test-agent"));

        assertEquals("REFRESH_TOKEN_REUSED", exception.getErrorCode());
        assertTrue(sibling.isRevoked());
    }

    private RefreshToken token(UserAccount user, UUID familyId) {
        return new RefreshToken(UUID.randomUUID(), user, "hash-" + UUID.randomUUID(), familyId, Instant.now(), Instant.now().plusSeconds(3600), null, null);
    }
}
