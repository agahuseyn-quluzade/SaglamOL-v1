package az.saglamol.airisk.service;

import az.saglamol.airisk.client.ClaimDetailResponse;
import az.saglamol.airisk.client.ClaimInternalClient;
import az.saglamol.airisk.client.FraudInternalClient;
import az.saglamol.airisk.client.FraudSummaryResponse;
import az.saglamol.airisk.client.PolicyDetailResponse;
import az.saglamol.airisk.client.PolicyInternalClient;
import az.saglamol.airisk.config.AiRiskProperties;
import az.saglamol.airisk.dto.AiRiskAssessmentResponse;
import az.saglamol.airisk.dto.AiRiskSummaryResponse;
import az.saglamol.airisk.entity.AiRequestLog;
import az.saglamol.airisk.entity.AiRiskAssessment;
import az.saglamol.airisk.entity.AiRiskAssessmentStatus;
import az.saglamol.airisk.entity.OutboxEvent;
import az.saglamol.airisk.entity.RiskLevel;
import az.saglamol.airisk.exception.AiRiskException;
import az.saglamol.airisk.mapper.AiRiskMapper;
import az.saglamol.airisk.model.AiRiskPayload;
import az.saglamol.airisk.model.RiskModelResult;
import az.saglamol.airisk.repository.AiRequestLogRepository;
import az.saglamol.airisk.repository.AiRiskAssessmentRepository;
import az.saglamol.common.events.risk.RiskAnalysisCompletedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AiRiskAssessmentService {

    private final AiRiskAssessmentRepository assessmentRepository;
    private final AiRequestLogRepository requestLogRepository;
    private final ClaimInternalClient claimClient;
    private final PolicyInternalClient policyClient;
    private final FraudInternalClient fraudClient;
    private final AiPromptBuilder promptBuilder;
    private final ExternalAiRiskModelClient externalClient;
    private final FallbackRiskModelClient fallbackClient;
    private final AiRiskProperties properties;
    private final AiRiskAccessService accessService;
    private final AiRiskMapper mapper;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public AiRiskAssessmentService(
            AiRiskAssessmentRepository assessmentRepository,
            AiRequestLogRepository requestLogRepository,
            ClaimInternalClient claimClient,
            PolicyInternalClient policyClient,
            FraudInternalClient fraudClient,
            AiPromptBuilder promptBuilder,
            ExternalAiRiskModelClient externalClient,
            FallbackRiskModelClient fallbackClient,
            AiRiskProperties properties,
            AiRiskAccessService accessService,
            AiRiskMapper mapper,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.assessmentRepository = assessmentRepository;
        this.requestLogRepository = requestLogRepository;
        this.claimClient = claimClient;
        this.policyClient = policyClient;
        this.fraudClient = fraudClient;
        this.promptBuilder = promptBuilder;
        this.externalClient = externalClient;
        this.fallbackClient = fallbackClient;
        this.properties = properties;
        this.accessService = accessService;
        this.mapper = mapper;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public AiRiskAssessmentResponse assessClaim(AuthContext authContext, UUID claimId) {
        ClaimDetailResponse claim = claimClient.getClaim(claimId);
        accessService.requireCanAssess(authContext, claim.insuranceCompanyId());
        return response(runAssessment(claim), authContext);
    }

    @Transactional
    public AiRiskAssessmentResponse assessSubmittedClaim(ClaimDetailResponse claim) {
        return response(runAssessment(claim), null);
    }

    @Transactional(readOnly = true)
    public AiRiskAssessmentResponse getByClaim(AuthContext authContext, UUID claimId) {
        AiRiskAssessment assessment = assessmentRepository.findTopByClaimIdOrderByCreatedAtDesc(claimId)
                .orElseThrow(() -> new AiRiskException("AI_RISK_ASSESSMENT_NOT_FOUND", "AI risk assessment was not found"));
        accessService.requireCanView(authContext, assessment);
        return response(assessment, authContext);
    }

    @Transactional(readOnly = true)
    public AiRiskAssessmentResponse getByAssessmentId(AuthContext authContext, UUID assessmentId) {
        AiRiskAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AiRiskException("AI_RISK_ASSESSMENT_NOT_FOUND", "AI risk assessment was not found"));
        accessService.requireCanView(authContext, assessment);
        return response(assessment, authContext);
    }

    @Transactional(readOnly = true)
    public AiRiskSummaryResponse companySummary(AuthContext authContext, UUID companyId) {
        UUID scopedCompanyId = accessService.scopedCompany(authContext, companyId);
        List<AiRiskAssessment> assessments = assessmentRepository.findByInsuranceCompanyId(scopedCompanyId);
        long success = assessments.stream().filter(a -> a.getStatus() == AiRiskAssessmentStatus.SUCCESS).count();
        long fallback = assessments.stream().filter(a -> a.getStatus() == AiRiskAssessmentStatus.FALLBACK_USED).count();
        long failed = assessments.stream().filter(a -> a.getStatus() == AiRiskAssessmentStatus.FAILED).count();
        long completed = success + fallback;
        BigDecimal average = completed == 0 ? BigDecimal.ZERO : assessments.stream()
                .filter(a -> a.getStatus() == AiRiskAssessmentStatus.SUCCESS || a.getStatus() == AiRiskAssessmentStatus.FALLBACK_USED)
                .map(AiRiskAssessment::getRiskScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(completed), 4, RoundingMode.HALF_UP);
        return new AiRiskSummaryResponse(scopedCompanyId, assessments.size(), success, fallback, failed, average);
    }

    private AiRiskAssessment runAssessment(ClaimDetailResponse claim) {
        Instant now = Instant.now();
        AiRiskAssessment assessment = assessmentRepository.save(new AiRiskAssessment(
                UUID.randomUUID(),
                claim.id(),
                claim.insuranceCompanyId(),
                claim.patientProfileId(),
                claim.policyId(),
                claim.hospitalId(),
                claim.doctorProfileId(),
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                RiskLevel.LOW,
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                List.of(),
                null,
                AiRiskAssessmentStatus.PENDING,
                now,
                null
        ));
        try {
            AiRiskPayload payload = payload(claim);
            Instant started = Instant.now();
            RiskModelResult result = assessWithFallback(payload);
            long durationMs = Duration.between(started, Instant.now()).toMillis();
            requestLogRepository.save(new AiRequestLog(
                    UUID.randomUUID(),
                    claim.id(),
                    serialize(payload),
                    result.responsePayload(),
                    result.providerName(),
                    result.model(),
                    result.status().name(),
                    result.errorMessage(),
                    durationMs,
                    Instant.now()
            ));
            assessment.complete(result.riskScore(), result.riskLevel(), result.confidence(), result.reasons(),
                    result.rawProviderResponse(), result.status(), Instant.now());
            AiRiskAssessment saved = assessmentRepository.save(assessment);
            outboxEventService.saveEvent("AiRiskAssessment", saved.getId(), RiskAnalysisCompletedEvent.class.getSimpleName(),
                    new RiskAnalysisCompletedEvent(
                            saved.getClaimId(),
                            saved.getInsuranceCompanyId(),
                            saved.getId(),
                            saved.getRiskScore().doubleValue(),
                            saved.getRiskLevel().name(),
                            saved.getConfidence().doubleValue(),
                            saved.getReasons(),
                            Instant.now()
                    ));
            return saved;
        } catch (RuntimeException exception) {
            assessment.fail(Instant.now());
            assessmentRepository.save(assessment);
            throw exception;
        }
    }

    private RiskModelResult assessWithFallback(AiRiskPayload payload) {
        if (!properties.isEnabled()) {
            return fallbackClient.assess(payload);
        }
        try {
            return externalClient.assess(payload);
        } catch (RuntimeException exception) {
            RiskModelResult fallback = fallbackClient.assess(payload);
            return new RiskModelResult(
                    fallback.riskScore(),
                    fallback.riskLevel(),
                    fallback.confidence(),
                    fallback.reasons(),
                    fallback.rawProviderResponse(),
                    fallback.status(),
                    fallback.providerName(),
                    fallback.model(),
                    fallback.responsePayload(),
                    exception.getMessage()
            );
        }
    }

    private AiRiskPayload payload(ClaimDetailResponse claim) {
        PolicyDetailResponse policy = null;
        FraudSummaryResponse fraudSummary = null;
        try {
            policy = claim.policyId() == null ? null : policyClient.getPolicy(claim.policyId());
        } catch (RuntimeException ignored) {
            policy = null;
        }
        try {
            fraudSummary = fraudClient.companySummary(claim.insuranceCompanyId());
        } catch (RuntimeException ignored) {
            fraudSummary = null;
        }
        return promptBuilder.build(claim, policy, fraudSummary);
    }

    private AiRiskAssessmentResponse response(AiRiskAssessment assessment, AuthContext authContext) {
        AiRiskAssessmentResponse response = mapper.toResponse(assessment);
        if (authContext != null && accessService.canSeeRawProviderResponse(authContext)) {
            return response;
        }
        return new AiRiskAssessmentResponse(
                response.id(),
                response.claimId(),
                response.insuranceCompanyId(),
                response.patientProfileId(),
                response.policyId(),
                response.hospitalId(),
                response.doctorProfileId(),
                response.riskScore(),
                response.riskLevel(),
                response.confidence(),
                response.reasons(),
                null,
                response.status(),
                response.createdAt(),
                response.completedAt()
        );
    }

    private String serialize(Object value) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules().writeValueAsString(value);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize AI risk request", exception);
        }
    }
}
