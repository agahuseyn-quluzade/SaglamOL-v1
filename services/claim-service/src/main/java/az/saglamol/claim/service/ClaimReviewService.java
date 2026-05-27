package az.saglamol.claim.service;

import az.saglamol.claim.client.PolicyInternalClient;
import az.saglamol.claim.client.dto.ReleaseReservationRequest;
import az.saglamol.claim.dto.request.ReviewClaimRequest;
import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimDecision;
import az.saglamol.claim.entity.ClaimDecisionType;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.ClaimStatusHistory;
import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.claim.mapper.ClaimMapper;
import az.saglamol.claim.repository.ClaimDecisionRepository;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.ClaimStatusHistoryRepository;
import az.saglamol.common.events.claim.ClaimApprovedEvent;
import az.saglamol.common.events.claim.ClaimRejectedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class ClaimReviewService {

    private final ClaimService claimService;
    private final ClaimRepository claimRepository;
    private final ClaimDecisionRepository decisionRepository;
    private final ClaimStatusHistoryRepository statusHistoryRepository;
    private final PolicyInternalClient policyClient;
    private final ClaimAccessService accessService;
    private final ClaimMapper mapper;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public ClaimReviewService(
            ClaimService claimService,
            ClaimRepository claimRepository,
            ClaimDecisionRepository decisionRepository,
            ClaimStatusHistoryRepository statusHistoryRepository,
            PolicyInternalClient policyClient,
            ClaimAccessService accessService,
            ClaimMapper mapper,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.claimService = claimService;
        this.claimRepository = claimRepository;
        this.decisionRepository = decisionRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.policyClient = policyClient;
        this.accessService = accessService;
        this.mapper = mapper;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public ClaimResponse startReview(AuthContext authContext, UUID claimId) {
        Claim claim = claimService.requireClaim(claimId);
        accessService.requireCanReviewClaim(authContext, claim);
        claimService.requireStatus(claim, ClaimStatus.SUBMITTED);
        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        claim.startReview(now);
        statusHistoryRepository.save(history(claim, fromStatus, ClaimStatus.UNDER_REVIEW, authContext.userId(), "Review started", now));
        return mapper.toResponse(claimRepository.save(claim));
    }

    @Transactional
    public ClaimResponse requestMoreDocuments(AuthContext authContext, UUID claimId, String reason) {
        Claim claim = claimService.requireClaim(claimId);
        accessService.requireCanReviewClaim(authContext, claim);
        claimService.requireStatus(claim, ClaimStatus.UNDER_REVIEW);
        requireReason(reason);
        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        claim.requestMoreDocuments(reason, now);
        statusHistoryRepository.save(history(claim, fromStatus, ClaimStatus.NEEDS_MORE_DOCUMENTS, authContext.userId(), reason, now));
        return mapper.toResponse(claimRepository.save(claim));
    }

    @Transactional
    public ClaimResponse approveClaim(AuthContext authContext, UUID claimId, ReviewClaimRequest request) {
        Claim claim = claimService.requireClaim(claimId);
        accessService.requireCanReviewClaim(authContext, claim);
        claimService.requireStatus(claim, ClaimStatus.UNDER_REVIEW);
        BigDecimal approvedAmount = request.approvedAmount() == null ? claim.getCoveredAmount() : request.approvedAmount();
        if (approvedAmount.compareTo(claim.getCoveredAmount()) > 0) {
            throw new ClaimException("APPROVED_AMOUNT_EXCEEDS_COVERED", "Approved amount cannot exceed covered amount");
        }
        if (claim.getPolicyReservationId() != null) {
            policyClient.confirmReservation(claim.getPolicyReservationId());
        }
        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        String reason = request.reason() == null || request.reason().isBlank() ? "Approved" : request.reason();
        claim.approve(approvedAmount, reason, now);
        decisionRepository.save(new ClaimDecision(
                UUID.randomUUID(),
                claim.getId(),
                ClaimDecisionType.APPROVED,
                authContext.userId(),
                reason,
                approvedAmount,
                now
        ));
        statusHistoryRepository.save(history(claim, fromStatus, ClaimStatus.APPROVED, authContext.userId(), reason, now));
        outboxEventService.saveEvent("Claim", claim.getId(), ClaimApprovedEvent.class.getSimpleName(), new ClaimApprovedEvent(
                claim.getId(),
                claim.getInsuranceCompanyId(),
                claim.getPatientProfileId(),
                claim.getPolicyId(),
                approvedAmount,
                authContext.userId().toString(),
                now
        ));
        return mapper.toResponse(claimRepository.save(claim));
    }

    @Transactional
    public ClaimResponse rejectClaim(AuthContext authContext, UUID claimId, ReviewClaimRequest request) {
        requireReason(request.reason());
        Claim claim = claimService.requireClaim(claimId);
        accessService.requireCanReviewClaim(authContext, claim);
        claimService.requireStatus(claim, ClaimStatus.UNDER_REVIEW);
        if (claim.getPolicyReservationId() != null) {
            policyClient.releaseReservation(claim.getPolicyReservationId(), new ReleaseReservationRequest(request.reason()));
        }
        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        claim.reject(request.reason(), now);
        decisionRepository.save(new ClaimDecision(
                UUID.randomUUID(),
                claim.getId(),
                ClaimDecisionType.REJECTED,
                authContext.userId(),
                request.reason(),
                null,
                now
        ));
        statusHistoryRepository.save(history(claim, fromStatus, ClaimStatus.REJECTED, authContext.userId(), request.reason(), now));
        outboxEventService.saveEvent("Claim", claim.getId(), ClaimRejectedEvent.class.getSimpleName(), new ClaimRejectedEvent(
                claim.getId(),
                claim.getInsuranceCompanyId(),
                claim.getPatientProfileId(),
                request.reason(),
                authContext.userId().toString(),
                now
        ));
        return mapper.toResponse(claimRepository.save(claim));
    }

    @Transactional
    public ClaimResponse cancelClaim(AuthContext authContext, UUID claimId, String reason) {
        Claim claim = claimService.requireClaim(claimId);
        accessService.requireCanViewClaim(authContext, claim);
        if (claim.getStatus() != ClaimStatus.DRAFT && claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new ClaimException("INVALID_CLAIM_STATUS", "Only DRAFT or SUBMITTED claims can be cancelled");
        }
        if (claim.getPolicyReservationId() != null) {
            policyClient.releaseReservation(claim.getPolicyReservationId(), new ReleaseReservationRequest(reason));
        }
        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        claim.cancel(reason, now);
        statusHistoryRepository.save(history(claim, fromStatus, ClaimStatus.CANCELLED, authContext.userId(), reason, now));
        return mapper.toResponse(claimRepository.save(claim));
    }

    private ClaimStatusHistory history(Claim claim, ClaimStatus fromStatus, ClaimStatus toStatus,
                                       UUID changedBy, String reason, Instant now) {
        return new ClaimStatusHistory(UUID.randomUUID(), claim.getId(), fromStatus, toStatus, changedBy, reason, now);
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ClaimException("REASON_REQUIRED", "Reason is required");
        }
    }
}
