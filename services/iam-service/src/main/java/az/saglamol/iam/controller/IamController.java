package az.saglamol.iam.controller;

import az.saglamol.iam.dto.request.LoginRequest;
import az.saglamol.iam.dto.request.EmailLoginRequest;
import az.saglamol.iam.dto.request.LogoutRequest;
import az.saglamol.iam.dto.request.PhoneLoginRequest;
import az.saglamol.iam.dto.request.RefreshTokenRequest;
import az.saglamol.iam.dto.request.RegisterRequest;
import az.saglamol.iam.dto.request.RevokeRefreshTokenRequest;
import az.saglamol.iam.dto.response.MeResponse;
import az.saglamol.iam.dto.response.OperationResponse;
import az.saglamol.iam.dto.response.RegisterResponse;
import az.saglamol.iam.dto.response.TokenResponse;
import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.iam.service.IamApplicationService;
import az.saglamol.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam")
@Tag(name = "IAM Auth")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Validation or business error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflict",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class IamController {

    private final IamApplicationService iamApplicationService;

    public IamController(IamApplicationService iamApplicationService) {
        this.iamApplicationService = iamApplicationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a patient user")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return iamApplicationService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login by email or phone identifier")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return iamApplicationService.login(request, ipAddress(servletRequest), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/login/email")
    @Operation(summary = "Login by email")
    public TokenResponse loginWithEmail(@Valid @RequestBody EmailLoginRequest request, HttpServletRequest servletRequest) {
        return iamApplicationService.loginWithEmail(
                request,
                ipAddress(servletRequest),
                servletRequest.getHeader("User-Agent")
        );
    }

    @PostMapping("/login/phone")
    @Operation(summary = "Login by phone number")
    public TokenResponse loginWithPhone(@Valid @RequestBody PhoneLoginRequest request, HttpServletRequest servletRequest) {
        return iamApplicationService.loginWithPhone(
                request,
                ipAddress(servletRequest),
                servletRequest.getHeader("User-Agent")
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue a new access token")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
        return iamApplicationService.refresh(request, ipAddress(servletRequest), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke current refresh token")
    public OperationResponse logout(@Valid @RequestBody LogoutRequest request) {
        return iamApplicationService.logout(request);
    }

    @PostMapping("/refresh-tokens/revoke")
    @Operation(summary = "Revoke a refresh token")
    public OperationResponse revokeRefreshToken(@Valid @RequestBody RevokeRefreshTokenRequest request) {
        return iamApplicationService.revokeRefreshToken(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Return current authenticated user")
    public MeResponse me(@AuthenticationPrincipal JwtPrincipal principal) {
        return iamApplicationService.me(principal);
    }

    private String ipAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
