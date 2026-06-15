package az.saglamol.iam.service;

import az.saglamol.iam.entity.RefreshToken;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.exception.IamException;
import az.saglamol.iam.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration tokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${iam.refresh-token.days-to-live}") long daysToLive
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenTtl = Duration.ofDays(daysToLive);
    }

    @Transactional
    public IssuedRefreshToken issue(UserAccount user, String ipAddress, String userAgent) {
        return issue(user, UUID.randomUUID(), ipAddress, userAgent);
    }

    @Transactional
    public RotatedRefreshToken rotate(String rawRefreshToken, String ipAddress, String userAgent) {
        Instant now = Instant.now();
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new IamException("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));

        if (current.isRevoked()) {
            revokeFamily(current.getFamilyId(), now);
            throw new IamException("REFRESH_TOKEN_REUSED", "Refresh token reuse detected");
        }

        if (current.isExpired(now)) {
            current.revoke(now, null);
            throw new IamException("INVALID_REFRESH_TOKEN", "Refresh token is expired");
        }

        IssuedRefreshToken replacement = issue(current.getUser(), current.getFamilyId(), ipAddress, userAgent);
        current.revoke(now, replacement.entity().getId());
        return new RotatedRefreshToken(current.getUser(), replacement.rawToken());
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new IamException("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        if (!token.isRevoked()) {
            token.revoke(Instant.now(), null);
        }
    }

    @Transactional
    public void revokeActiveTokensForUser(UserAccount user) {
        Instant now = Instant.now();
        refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(user.getId())
                .forEach(token -> token.revoke(now, null));
    }

    private IssuedRefreshToken issue(UserAccount user, UUID familyId, String ipAddress, String userAgent) {
        String rawToken = generateRawToken();
        Instant now = Instant.now();
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                user,
                hash(rawToken),
                familyId,
                now,
                now.plus(tokenTtl),
                ipAddress,
                userAgent
        );
        return new IssuedRefreshToken(refreshTokenRepository.save(token), rawToken);
    }

    private void revokeFamily(UUID familyId, Instant revokedAt) {
        refreshTokenRepository.findAllByFamilyId(familyId).forEach(token -> {
            if (!token.isRevoked()) {
                token.revoke(revokedAt, null);
            }
        });
    }

    private String generateRawToken() {
        byte[] bytes = new byte[48];
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

    public record IssuedRefreshToken(RefreshToken entity, String rawToken) {
    }

    public record RotatedRefreshToken(UserAccount user, String rawToken) {
    }
}
