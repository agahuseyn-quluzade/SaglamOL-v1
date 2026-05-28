package az.saglamol.fraud.service;

import az.saglamol.common.events.fraud.FraudCheckCompletedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.fraud.client.ClaimDetailResponse;
import az.saglamol.fraud.client.ClaimInternalClient;
import az.saglamol.fraud.client.ClaimSummaryResponse;
import az.saglamol.fraud.client.DocumentHashBatchRequest;
import az.saglamol.fraud.client.HealthRecordInternalClient;
import az.saglamol.fraud.client.MedicalDocumentHashResponse;
import az.saglamol.fraud.client.PolicyDetailResponse;
import az.saglamol.fraud.client.PolicyInternalClient;
import az.saglamol.fraud.dto.FraudAssessmentDetailResponse;
import az.saglamol.fraud.dto.FraudSummaryResponse;
import az.saglamol.fraud.entity.DocumentHashIndex;
import az.saglamol.fraud.entity.FraudAssessment;
import az.saglamol.fraud.entity.FraudAssessmentStatus;
import az.saglamol.fraud.entity.FraudLevel;
import az.saglamol.fraud.entity.FraudSignal;
import az.saglamol.fraud.entity.OutboxEvent;
import az.saglamol.fraud.exception.FraudException;
import az.saglamol.fraud.mapper.FraudMapper;
import az.saglamol.fraud.repository.DocumentHashIndexRepository;
import az.saglamol.fraud.repository.FraudAssessmentRepository;
import az.saglamol.fraud.repository.FraudSignalRepository;
import az.saglamol.fraud.rule.FraudContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class FraudAssessmentService {

    private final FraudAssessmentRepository assessmentRepository;
    private final FraudSignalRepository signalRepository;
    private final DocumentHashIndexRepository hashIndexRepository;
    private final ClaimInternalClient claimClient;
    private final HealthRecordInternalClient healthRecordClient;
    private final PolicyInternalClient policyClient;
    private final FraudScoringService scoringService;
    private final FraudAccessService accessService;
    private final FraudMapper mapper;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public FraudAssessmentService(
            FraudAssessmentRepository assessmentRepository,
            FraudSignalRepository signalRepository,
            DocumentHashIndexRepository hashIndexRepository,
            ClaimInternalClient claimClient,
            HealthRecordInternalClient healthRecordClient,
            PolicyInternalClient policyClient,
            FraudScoringService scoringService,
            FraudAccessService accessService,
            FraudMapper mapper,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.assessmentRepository = assessmentRepository;
        this.signalRepository = signalRepository;
        this.hashIndexRepository = hashIndexRepository;
        this.claimClient = claimClient;
        this.healthRecordClient = healthRecordClient;
        this.policyClient = policyClient;
        this.scoringService = scoringService;
        this.accessService = accessService;
        this.mapper = mapper;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public FraudAssessmentDetailResponse checkClaim(AuthContext authContext, UUID claimId) {
        ClaimDetailResponse claim = claimClient.getClaim(claimId);
        accessService.requireCanRunCheck(authContext, claim.insuranceCompanyId(), claim.hospitalId());
        return runAssessment(claim);
    }

    @Transactional
    public FraudAssessmentDetailResponse checkSubmittedClaim(ClaimDetailResponse claim) {
        return runAssessment(claim);
    }

    @Transactional(readOnly = true)
    public FraudAssessmentDetailResponse getByClaim(AuthContext authContext, UUID claimId) {
        FraudAssessment assessment = assessmentRepository.findTopByClaimIdOrderByCreatedAtDesc(claimId)
                .orElseThrow(() -> new FraudException("FRAUD_ASSESSMENT_NOT_FOUND", "Fraud assessment was not found"));
        accessService.requireCanView(authContext, assessment);
        return detail(assessment);
    }

    @Transactional(readOnly = true)
    public FraudAssessmentDetailResponse getByAssessmentId(AuthContext authContext, UUID assessmentId) {
        FraudAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new FraudException("FRAUD_ASSESSMENT_NOT_FOUND", "Fraud assessment was not found"));
        accessService.requireCanView(authContext, assessment);
        return detail(assessment);
    }

    @Transactional(readOnly = true)
    public FraudSummaryResponse companySummary(AuthContext authContext, UUID companyId) {
        UUID scopedCompanyId = accessService.scopedCompany(authContext, companyId);
        List<FraudAssessment> assessments = assessmentRepository.findByInsuranceCompanyId(scopedCompanyId);
        return summary(scopedCompanyId, assessments);
    }

    @Transactional(readOnly = true)
    public FraudSummaryResponse hospitalSummary(AuthContext authContext, UUID hospitalId) {
        UUID scopedHospitalId = accessService.scopedHospital(authContext, hospitalId);
        List<FraudAssessment> assessments = assessmentRepository.findByHospitalId(scopedHospitalId);
        return summary(scopedHospitalId, assessments);
    }

    private FraudAssessmentDetailResponse runAssessment(ClaimDetailResponse claim) {
        Instant now = Instant.now();
        FraudAssessment assessment = assessmentRepository.save(new FraudAssessment(
                UUID.randomUUID(),
                claim.id(),
                claim.insuranceCompanyId(),
                claim.patientProfileId(),
                claim.hospitalId(),
                claim.doctorProfileId(),
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                FraudLevel.LOW,
                false,
                FraudAssessmentStatus.PENDING,
                now,
                null
        ));
        try {
            FraudContext context = context(claim);
            FraudScoringService.FraudScoringResult scoring = scoringService.score(context);
            assessment.complete(scoring.fraudScore(), scoring.fraudLevel(), scoring.manualReviewRequired(), Instant.now());
            FraudAssessment saved = assessmentRepository.save(assessment);
            List<FraudSignal> signals = scoring.signals().stream()
                    .map(result -> signalRepository.save(new FraudSignal(
                            UUID.randomUUID(),
                            saved.getId(),
                            result.signalType(),
                            result.severity(),
                            result.scoreImpact(),
                            result.message(),
                            Instant.now()
                    )))
                    .toList();
            persistDocumentHashes(context);
            outboxEventService.saveEvent("FraudAssessment", saved.getId(), FraudCheckCompletedEvent.class.getSimpleName(),
                    new FraudCheckCompletedEvent(
                            claim.id(),
                            claim.insuranceCompanyId(),
                            saved.getId(),
                            saved.getFraudScore().doubleValue(),
                            saved.getFraudLevel().name(),
                            signals.stream().map(FraudSignal::getMessage).toList(),
                            !saved.isManualReviewRequired(),
                            Instant.now()
                    ));
            return new FraudAssessmentDetailResponse(mapper.toResponse(saved), signals.stream().map(mapper::toResponse).toList());
        } catch (RuntimeException exception) {
            assessment.fail(Instant.now());
            assessmentRepository.save(assessment);
            throw exception;
        }
    }

    private FraudContext context(ClaimDetailResponse claim) {
        PolicyDetailResponse policy = claim.policyId() == null ? null : policyClient.getPolicy(claim.policyId());
        LocalDate policyStartDate = policy == null ? null : policy.startDate();
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<String> documentHashes = documentHashes(claim);
        boolean hasDuplicateDocument = documentHashes.stream()
                .anyMatch(hash -> hashIndexRepository.existsBySha256HashAndClaimIdNot(hash, claim.id()));
        return new FraudContext(
                claim.id(),
                claim.policyId(),
                claim.insuranceCompanyId(),
                claim.patientProfileId(),
                claim.hospitalId(),
                claim.doctorProfileId(),
                claim.claimAmount() == null ? BigDecimal.ZERO : claim.claimAmount(),
                claim.treatmentDate(),
                policyStartDate,
                assessmentRepository.countByPatientProfileIdAndCreatedAtAfter(claim.patientProfileId(), thirtyDaysAgo),
                averageClaimAmount(claim.insuranceCompanyId()),
                claim.hospitalId() == null ? 0 : assessmentRepository.countByHospitalIdAndFraudLevelInAndCreatedAtAfter(
                        claim.hospitalId(), List.of(FraudLevel.HIGH, FraudLevel.CRITICAL), thirtyDaysAgo),
                claim.doctorProfileId() == null ? 0 : assessmentRepository.countByDoctorProfileIdAndFraudLevelInAndCreatedAtAfter(
                        claim.doctorProfileId(), List.of(FraudLevel.HIGH, FraudLevel.CRITICAL), thirtyDaysAgo),
                documentHashes,
                hasDuplicateDocument
        );
    }

    private List<String> documentHashes(ClaimDetailResponse claim) {
        try {
            List<UUID> documentIds = claim.documentIds();
            if (documentIds == null || documentIds.isEmpty()) {
                documentIds = healthRecordClient.documentsByClaim(claim.id()).stream()
                        .map(document -> document.id())
                        .toList();
            }
            if (documentIds.isEmpty()) {
                return List.of();
            }
            return healthRecordClient.documentHashes(new DocumentHashBatchRequest(documentIds)).stream()
                    .map(MedicalDocumentHashResponse::sha256Hash)
                    .filter(hash -> hash != null && !hash.isBlank())
                    .distinct()
                    .toList();
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private void persistDocumentHashes(FraudContext context) {
        context.documentHashes().forEach(hash -> {
            if (hashIndexRepository.findBySha256Hash(hash).stream().noneMatch(existing -> existing.getClaimId().equals(context.claimId()))) {
                hashIndexRepository.save(new DocumentHashIndex(
                        UUID.randomUUID(),
                        hash,
                        context.claimId(),
                        context.patientProfileId(),
                        context.insuranceCompanyId(),
                        context.hospitalId(),
                        Instant.now()
                ));
            }
        });
    }

    private BigDecimal averageClaimAmount(UUID companyId) {
        try {
            List<BigDecimal> amounts = claimClient.claimsByCompany(companyId, PageRequest.of(0, 100)).getContent().stream()
                    .map(ClaimSummaryResponse::claimAmount)
                    .filter(amount -> amount != null && amount.signum() > 0)
                    .toList();
            if (amounts.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal total = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            return total.divide(BigDecimal.valueOf(amounts.size()), 4, RoundingMode.HALF_UP);
        } catch (RuntimeException exception) {
            return BigDecimal.ZERO;
        }
    }

    private FraudAssessmentDetailResponse detail(FraudAssessment assessment) {
        return new FraudAssessmentDetailResponse(
                mapper.toResponse(assessment),
                signalRepository.findByFraudAssessmentId(assessment.getId()).stream().map(mapper::toResponse).toList()
        );
    }

    private FraudSummaryResponse summary(UUID scopeId, List<FraudAssessment> assessments) {
        long completed = assessments.stream().filter(a -> a.getStatus() == FraudAssessmentStatus.COMPLETED).count();
        long pending = assessments.stream().filter(a -> a.getStatus() == FraudAssessmentStatus.PENDING).count();
        long failed = assessments.stream().filter(a -> a.getStatus() == FraudAssessmentStatus.FAILED).count();
        BigDecimal average = completed == 0 ? BigDecimal.ZERO : assessments.stream()
                .filter(a -> a.getStatus() == FraudAssessmentStatus.COMPLETED)
                .map(FraudAssessment::getFraudScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(completed), 4, RoundingMode.HALF_UP);
        return new FraudSummaryResponse(scopeId, assessments.size(), completed, pending, failed, average);
    }
}
