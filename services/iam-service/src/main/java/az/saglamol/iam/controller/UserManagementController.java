package az.saglamol.iam.controller;

import az.saglamol.common.exception.ErrorResponse;
import az.saglamol.iam.dto.request.AssignRoleRequest;
import az.saglamol.iam.dto.request.UpdateUserStatusRequest;
import az.saglamol.iam.dto.response.UserResponse;
import az.saglamol.iam.service.UserManagementService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/iam/users")
@Tag(name = "IAM User Management")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Validation or business error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication is required",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Admin role is required",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User or role was not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @Operation(summary = "List users")
    public List<UserResponse> users() {
        return userManagementService.users();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public UserResponse user(@PathVariable UUID id) {
        return userManagementService.user(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by email or phone number")
    public List<UserResponse> search(@RequestParam("q") @NotBlank String query) {
        return userManagementService.search(query);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change user status")
    public UserResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return userManagementService.changeStatus(id, request);
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Assign role to user")
    public UserResponse assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {
        return userManagementService.assignRole(id, request);
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    @Operation(summary = "Remove role from user")
    public UserResponse removeRole(@PathVariable UUID id, @PathVariable String roleName) {
        return userManagementService.removeRole(id, roleName);
    }
}
