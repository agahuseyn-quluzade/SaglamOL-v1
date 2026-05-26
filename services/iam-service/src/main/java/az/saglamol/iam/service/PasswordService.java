package az.saglamol.iam.service;

import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.iam.dto.request.ChangePasswordRequest;
import az.saglamol.iam.dto.request.PasswordResetConfirmRequest;
import az.saglamol.iam.dto.request.PasswordResetRequest;
import az.saglamol.iam.dto.response.OperationResponse;
import az.saglamol.iam.dto.response.PasswordResetRequestedResponse;
import az.saglamol.iam.entity.PasswordResetToken;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.exception.IamException;
import az.saglamol.iam.repository.PasswordResetTokenRepository;
import az.saglamol.iam.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PasswordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordService.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordService(
            UserAccountRepository userAccountRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public OperationResponse changePassword(JwtPrincipal principal, ChangePasswordRequest request) {
        UserAccount user = userAccountRepository.findById(principal.userId())
                .orElseThrow(() -> new IamException("USER_NOT_FOUND", "User was not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IamException("INVALID_CREDENTIALS", "Current password is invalid");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.revokeActiveTokensForUser(user);
        return new OperationResponse("PASSWORD_CHANGED");
    }

    @Transactional
    public PasswordResetRequestedResponse requestReset(PasswordResetRequest request) {
        findByIdentifier(request.identifier()).ifPresent(user -> {
            String rawToken = generateRawToken();
            Instant now = Instant.now();
            passwordResetTokenRepository.save(new PasswordResetToken(
                    UUID.randomUUID(),
                    user,
                    hash(rawToken),
                    now,
                    now.plusSeconds(900)));
            LOGGER.info("Mock password reset token issued for userId={}", user.getId());
        });
        return new PasswordResetRequestedResponse("RESET_REQUESTED", "If the account exists, a reset token was issued");
    }

    @Transactional
    public OperationResponse confirmReset(PasswordResetConfirmRequest request) {
        Instant now = Instant.now();
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(request.resetToken()))
                .orElseThrow(() -> new IamException("INVALID_RESET_TOKEN", "Password reset token is invalid"));
        if (token.isUsed() || token.isExpired(now)) {
            throw new IamException("INVALID_RESET_TOKEN", "Password reset token is invalid");
        }
        token.getUser().changePassword(passwordEncoder.encode(request.newPassword()));
        token.markUsed(now);
        refreshTokenService.revokeActiveTokensForUser(token.getUser());
        return new OperationResponse("PASSWORD_RESET");
    }

    private java.util.Optional<UserAccount> findByIdentifier(String identifier) {
        String normalized = identifier.trim();
        if (normalized.contains("@")) {
            return userAccountRepository.findByEmailIgnoreCase(normalized.toLowerCase());
        }
        return userAccountRepository.findByPhoneNumber(normalized.replace(" ", "").replace("-", ""));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
