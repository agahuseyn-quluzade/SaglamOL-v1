package az.saglamol.iam.service;

import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.iam.dto.request.ChangePasswordRequest;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.entity.UserStatus;
import az.saglamol.iam.repository.PasswordResetTokenRepository;
import az.saglamol.iam.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordServiceTest {

    @Test
    void changePasswordUpdatesHashAndRevokesActiveRefreshTokens() {
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        PasswordResetTokenRepository passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        PasswordService service = new PasswordService(userAccountRepository, passwordResetTokenRepository, passwordEncoder, refreshTokenService);

        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount(userId, "user@gmail.com", null, "old-hash", UserStatus.ACTIVE, Instant.now());
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hash");

        var response = service.changePassword(
                new JwtPrincipal(userId, "user@gmail.com", null, List.of("PATIENT")),
                new ChangePasswordRequest("OldPassword123", "NewPassword123")
        );

        assertEquals("PASSWORD_CHANGED", response.status());
        assertEquals("new-hash", user.getPasswordHash());
        verify(refreshTokenService).revokeActiveTokensForUser(user);
    }
}
