package az.saglamol.iam.service;

import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.iam.dto.request.EmailLoginRequest;
import az.saglamol.iam.dto.request.PhoneLoginRequest;
import az.saglamol.iam.dto.request.RefreshTokenRequest;
import az.saglamol.iam.dto.request.RegisterRequest;
import az.saglamol.iam.entity.RefreshToken;
import az.saglamol.iam.entity.Role;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.entity.UserStatus;
import az.saglamol.iam.repository.RoleRepository;
import az.saglamol.iam.repository.UserAccountRepository;
import az.saglamol.iam.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IamApplicationServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private IamApplicationService service;

    @Test
    void registerCreatesActivePatientUser() {
        Role patient = new Role(UUID.randomUUID(), RoleConstants.PATIENT, "Patient");
        when(roleRepository.findByCode(RoleConstants.PATIENT)).thenReturn(Optional.of(patient));
        when(passwordEncoder.encode("Password123")).thenReturn("hash");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.register(new RegisterRequest("USER@GMAIL.COM", "+994501234567", "Password123"));

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertEquals("user@gmail.com", captor.getValue().getEmail());
        assertEquals(UserStatus.ACTIVE, captor.getValue().getStatus());
        assertTrue(captor.getValue().getRoles().contains(patient));
        assertEquals("ACTIVE", response.status());
    }

    @Test
    void emailLoginIssuesTokens() {
        UserAccount user = activeUser("user@gmail.com", "+994501234567");
        when(userAccountRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(refreshTokenService.issue(user, "127.0.0.1", "test-agent"))
                .thenReturn(new RefreshTokenService.IssuedRefreshToken(refreshToken(user), "refresh-1"));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-1");
        when(jwtTokenProvider.accessTokenTtlSeconds()).thenReturn(900L);

        var response = service.loginWithEmail(new EmailLoginRequest("user@gmail.com", "Password123"), "127.0.0.1", "test-agent");

        assertEquals("access-1", response.accessToken());
        assertEquals("refresh-1", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void phoneLoginIssuesTokens() {
        UserAccount user = activeUser("user@gmail.com", "+994501234567");
        when(userAccountRepository.findByPhoneNumber("+994501234567")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(refreshTokenService.issue(user, "127.0.0.1", "test-agent"))
                .thenReturn(new RefreshTokenService.IssuedRefreshToken(refreshToken(user), "refresh-1"));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-1");

        var response = service.loginWithPhone(new PhoneLoginRequest("+994501234567", "Password123"), "127.0.0.1", "test-agent");

        assertEquals("access-1", response.accessToken());
        assertEquals("refresh-1", response.refreshToken());
    }

    @Test
    void refreshRotatesRefreshToken() {
        UserAccount user = activeUser("user@gmail.com", null);
        when(refreshTokenService.rotate("old-refresh", "127.0.0.1", "test-agent"))
                .thenReturn(new RefreshTokenService.RotatedRefreshToken(user, "new-refresh"));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("new-access");

        var response = service.refresh(new RefreshTokenRequest("old-refresh"), "127.0.0.1", "test-agent");

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        assertNotEquals("old-refresh", response.refreshToken());
    }

    @Test
    void meReturnsPrincipalData() {
        UUID userId = UUID.randomUUID();
        var response = service.me(new JwtPrincipal(userId, "user@gmail.com", "+994501234567", List.of("PATIENT")));

        assertEquals(userId, response.userId());
        assertEquals(List.of("PATIENT"), response.roles());
    }

    private UserAccount activeUser(String email, String phoneNumber) {
        UserAccount user = new UserAccount(UUID.randomUUID(), email, phoneNumber, "hash", UserStatus.ACTIVE, Instant.now());
        user.addRole(new Role(UUID.randomUUID(), RoleConstants.PATIENT, "Patient"));
        return user;
    }

    private RefreshToken refreshToken(UserAccount user) {
        return new RefreshToken(UUID.randomUUID(), user, "hash", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(3600), "127.0.0.1", "test-agent");
    }
}
