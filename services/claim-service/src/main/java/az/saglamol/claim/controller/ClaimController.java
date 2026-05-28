package az.saglamol.claim.controller;

import az.saglamol.claim.dto.request.AttachClaimDocumentRequest;
import az.saglamol.claim.dto.request.ClaimItemRequest;
import az.saglamol.claim.dto.request.ClaimSearchFilters;
import az.saglamol.claim.dto.request.CreateClaimRequest;
import az.saglamol.claim.dto.response.ClaimItemResponse;
import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.dto.response.ClaimSummaryResponse;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.service.ClaimService;
import az.saglamol.common.security.AuthContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Claims", description = "Claim creation, document attachment, submission and search")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful claim operation")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @Operation(summary = "Create draft claim")
    public ClaimResponse createClaim(@Valid @RequestBody CreateClaimRequest request) {
        return claimService.createClaim(AuthContextHolder.getRequired(), request);
    }

    @PostMapping("/{claimId}/items")
    @Operation(summary = "Add item to a draft claim")
    public ClaimItemResponse addItem(@PathVariable UUID claimId, @Valid @RequestBody ClaimItemRequest request) {
        return claimService.addClaimItem(AuthContextHolder.getRequired(), claimId, request);
    }

    @PostMapping("/{claimId}/documents")
    @Operation(summary = "Attach document reference to a draft claim")
    public void attachDocument(@PathVariable UUID claimId, @Valid @RequestBody AttachClaimDocumentRequest request) {
        claimService.attachDocument(AuthContextHolder.getRequired(), claimId, request);
    }

    @PostMapping("/{claimId}/submit")
    @Operation(summary = "Submit a draft claim")
    public ClaimResponse submit(@PathVariable UUID claimId) {
        return claimService.submitClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/{claimId}")
    @Operation(summary = "Get claim by ID")
    public ClaimResponse claim(@PathVariable UUID claimId) {
        return claimService.getClaimById(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/my")
    @Operation(summary = "Get current patient's claims")
    public Page<ClaimSummaryResponse> myClaims(Pageable pageable) {
        return claimService.getMyClaims(AuthContextHolder.getRequired(), pageable);
    }

    @GetMapping
    @Operation(summary = "Search claims by scoped filters")
    public Page<ClaimSummaryResponse> search(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID patientProfileId,
            @RequestParam(required = false) UUID hospitalId,
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable
    ) {
        return claimService.searchClaims(
                AuthContextHolder.getRequired(),
                new ClaimSearchFilters(companyId, patientProfileId, hospitalId, status, fromDate, toDate),
                pageable
        );
    }
}
