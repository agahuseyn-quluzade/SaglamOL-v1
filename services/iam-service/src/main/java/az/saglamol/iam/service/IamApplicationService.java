package az.saglamol.iam.service;

import az.saglamol.common.security.RoleConstants;
import az.saglamol.iam.dto.request.EmailLoginRequest;
import az.saglamol.iam.dto.request.LoginRequest;
import az.saglamol.iam.dto.request.PhoneLoginRequest;
import az.saglamol.iam.dto.request.RefreshTokenRequest;
import az.saglamol.iam.dto.request.RegisterRequest;
import az.saglamol.iam.dto.request.LogoutRequest;
import az.saglamol.iam.dto.request.RevokeRefreshTokenRequest;
import az.saglamol.iam.dto.response.MeResponse;
import az.saglamol.iam.dto.response.OperationResponse;
import az.saglamol.iam.dto.response.RegisterResponse;
import az.saglamol.iam.dto.response.TokenResponse;
import az.saglamol.iam.entity.Role;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.entity.UserStatus;
import az.saglamol.iam.exception.IamException;
import az.saglamol.iam.repository.RoleRepository;
import az.saglamol.iam.repository.UserAccountRepository;
import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.iam.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class IamApplicationService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public IamApplicationService(
            UserAccountRepository userAccountRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String phoneNumber = normalizePhoneNumber(request.phoneNumber());
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new IamException("EMAIL_ALREADY_EXISTS", "Email already exists");
        }
        if (phoneNumber != null && userAccountRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IamException("PHONE_ALREADY_EXISTS", "Phone number already exists");
        }

        Role patientRole = roleRepository.findByCode(RoleConstants.PATIENT)
                .orElseThrow(() -> new IamException("ROLE_NOT_FOUND", "Default patient role is missing"));
        UserAccount user = new UserAccount(
                UUID.randomUUID(),
                email,
                phoneNumber,
                passwordEncoder.encode(request.password()),
                UserStatus.ACTIVE,
                Instant.now()
        );
        user.addRole(patientRole);

        UserAccount saved = userAccountRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getStatus().name());
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        UserAccount user = findByIdentifier(request.identifier());
        return login(user, request.password(), ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse loginWithEmail(EmailLoginRequest request, String ipAddress, String userAgent) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new IamException("INVALID_CREDENTIALS", "Email or password is invalid"));
        return login(user, request.password(), ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse loginWithPhone(PhoneLoginRequest request, String ipAddress, String userAgent) {
        UserAccount user = userAccountRepository.findByPhoneNumber(normalizePhoneNumber(request.phoneNumber()))
                .orElseThrow(() -> new IamException("INVALID_CREDENTIALS", "Phone number or password is invalid"));
        return login(user, request.password(), ipAddress, userAgent);
    }

    private TokenResponse login(UserAccount user, String password, String ipAddress, String userAgent) {
        if (!passwordEncoder.matches(password, user.getPasswordHash()) || user.getStatus() != UserStatus.ACTIVE) {
            throw new IamException("INVALID_CREDENTIALS", "Login credentials are invalid");
        }

        user.markLoggedIn(Instant.now());
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user, ipAddress, userAgent);
        return tokenResponse(user, refreshToken.rawToken());
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(
                request.refreshToken(),
                ipAddress,
                userAgent
        );
        return tokenResponse(rotated.user(), rotated.rawToken());
    }

    @Transactional
    public OperationResponse logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return new OperationResponse("LOGGED_OUT");
    }

    @Transactional
    public OperationResponse revokeRefreshToken(RevokeRefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return new OperationResponse("REFRESH_TOKEN_REVOKED");
    }

    public MeResponse me(JwtPrincipal principal) {
        return new MeResponse(
                principal.userId(),
                principal.email(),
                principal.phoneNumber(),
                principal.roles().stream().sorted().toList()
        );
    }

    private TokenResponse tokenResponse(UserAccount user, String refreshToken) {
        return new TokenResponse(
                jwtTokenProvider.generateAccessToken(user),
                refreshToken,
                "Bearer",
                jwtTokenProvider.accessTokenTtlSeconds()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private UserAccount findByIdentifier(String identifier) {
        String normalized = identifier.trim();
        if (normalized.contains("@")) {
            return userAccountRepository.findByEmailIgnoreCase(normalizeEmail(normalized))
                    .orElseThrow(() -> new IamException("INVALID_CREDENTIALS", "Identifier or password is invalid"));
        }

        String phoneNumber = normalizePhoneNumber(normalized);
        if (phoneNumber == null) {
            throw new IamException("INVALID_CREDENTIALS", "Identifier or password is invalid");
        }
        return userAccountRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IamException("INVALID_CREDENTIALS", "Identifier or password is invalid"));
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return phoneNumber.trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");
    }
}
