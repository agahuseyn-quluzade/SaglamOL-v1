package az.saglamol.iam.controller;

import az.saglamol.common.security.JwtPrincipal;
import az.saglamol.common.exception.ErrorResponse;
import az.saglamol.iam.dto.request.ChangePasswordRequest;
import az.saglamol.iam.dto.request.PasswordResetConfirmRequest;
import az.saglamol.iam.dto.request.PasswordResetRequest;
import az.saglamol.iam.dto.response.OperationResponse;
import az.saglamol.iam.dto.response.PasswordResetRequestedResponse;
import az.saglamol.iam.service.PasswordService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam/password")
@Tag(name = "IAM Password")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Validation or business error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/change")
    @Operation(summary = "Change own password")
    public OperationResponse changePassword(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return passwordService.changePassword(principal, request);
    }

    @PostMapping("/reset-request")
    @Operation(summary = "Request password reset token")
    public PasswordResetRequestedResponse requestReset(@Valid @RequestBody PasswordResetRequest request) {
        return passwordService.requestReset(request);
    }

    @PostMapping("/reset-confirm")
    @Operation(summary = "Confirm password reset with token")
    public OperationResponse confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return passwordService.confirmReset(request);
    }
}
