package az.saglamol.claim.controller;

import az.saglamol.claim.dto.request.ReviewClaimRequest;
import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.service.ClaimReviewService;
import az.saglamol.common.security.AuthContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims/{claimId}/review")
@Tag(name = "Claim Review", description = "Claim review state machine operations")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful claim review operation")
public class ClaimReviewController {

    private final ClaimReviewService reviewService;

    public ClaimReviewController(ClaimReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/start")
    @Operation(summary = "Start claim review")
    public ClaimResponse startReview(@PathVariable UUID claimId) {
        return reviewService.startReview(AuthContextHolder.getRequired(), claimId);
    }

    @PostMapping("/more-documents")
    @Operation(summary = "Request more documents")
    public ClaimResponse requestMoreDocuments(@PathVariable UUID claimId, @RequestParam String reason) {
        return reviewService.requestMoreDocuments(AuthContextHolder.getRequired(), claimId, reason);
    }

    @PostMapping("/approve")
    @Operation(summary = "Approve reviewed claim")
    public ClaimResponse approve(@PathVariable UUID claimId, @Valid @RequestBody ReviewClaimRequest request) {
        return reviewService.approveClaim(AuthContextHolder.getRequired(), claimId, request);
    }

    @PostMapping("/reject")
    @Operation(summary = "Reject reviewed claim")
    public ClaimResponse reject(@PathVariable UUID claimId, @Valid @RequestBody ReviewClaimRequest request) {
        return reviewService.rejectClaim(AuthContextHolder.getRequired(), claimId, request);
    }

    @PutMapping("/cancel")
    @Operation(summary = "Cancel draft or submitted claim")
    public ClaimResponse cancel(@PathVariable UUID claimId, @RequestParam(required = false) String reason) {
        return reviewService.cancelClaim(AuthContextHolder.getRequired(), claimId, reason);
    }
}
