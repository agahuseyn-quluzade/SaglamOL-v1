package az.saglamol.claim.controller;

import az.saglamol.claim.dto.request.ReviewClaimRequest;
import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.service.ClaimReviewService;
import az.saglamol.common.security.AuthContextHolder;
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
public class ClaimReviewController {

    private final ClaimReviewService reviewService;

    public ClaimReviewController(ClaimReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/start")
    public ClaimResponse startReview(@PathVariable UUID claimId) {
        return reviewService.startReview(AuthContextHolder.getRequired(), claimId);
    }

    @PostMapping("/more-documents")
    public ClaimResponse requestMoreDocuments(@PathVariable UUID claimId, @RequestParam String reason) {
        return reviewService.requestMoreDocuments(AuthContextHolder.getRequired(), claimId, reason);
    }

    @PostMapping("/approve")
    public ClaimResponse approve(@PathVariable UUID claimId, @Valid @RequestBody ReviewClaimRequest request) {
        return reviewService.approveClaim(AuthContextHolder.getRequired(), claimId, request);
    }

    @PostMapping("/reject")
    public ClaimResponse reject(@PathVariable UUID claimId, @Valid @RequestBody ReviewClaimRequest request) {
        return reviewService.rejectClaim(AuthContextHolder.getRequired(), claimId, request);
    }

    @PutMapping("/cancel")
    public ClaimResponse cancel(@PathVariable UUID claimId, @RequestParam(required = false) String reason) {
        return reviewService.cancelClaim(AuthContextHolder.getRequired(), claimId, reason);
    }
}
