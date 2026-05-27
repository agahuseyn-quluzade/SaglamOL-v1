package az.saglamol.healthrecord.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.healthrecord.dto.request.CreateHealthRecordRequest;
import az.saglamol.healthrecord.dto.request.CreateTreatmentRequest;
import az.saglamol.healthrecord.dto.response.HealthRecordResponse;
import az.saglamol.healthrecord.dto.response.TreatmentResponse;
import az.saglamol.healthrecord.entity.HealthAccessLog;
import az.saglamol.healthrecord.entity.HealthRecord;
import az.saglamol.healthrecord.entity.HealthRecordStatus;
import az.saglamol.healthrecord.entity.Treatment;
import az.saglamol.healthrecord.exception.HealthRecordException;
import az.saglamol.healthrecord.mapper.HealthRecordMapper;
import az.saglamol.healthrecord.repository.HealthAccessLogRepository;
import az.saglamol.healthrecord.repository.HealthRecordRepository;
import az.saglamol.healthrecord.repository.TreatmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final TreatmentRepository treatmentRepository;
    private final HealthAccessLogRepository accessLogRepository;
    private final HealthRecordAccessService accessService;
    private final HealthRecordMapper mapper;

    public HealthRecordService(
            HealthRecordRepository healthRecordRepository,
            TreatmentRepository treatmentRepository,
            HealthAccessLogRepository accessLogRepository,
            HealthRecordAccessService accessService,
            HealthRecordMapper mapper
    ) {
        this.healthRecordRepository = healthRecordRepository;
        this.treatmentRepository = treatmentRepository;
        this.accessLogRepository = accessLogRepository;
        this.accessService = accessService;
        this.mapper = mapper;
    }

    @Transactional
    public HealthRecordResponse createHealthRecord(AuthContext authContext, CreateHealthRecordRequest request) {
        accessService.requireCanCreateRecord(authContext, request.patientProfileId(), request.doctorProfileId(), request.hospitalId());
        Instant now = Instant.now();
        HealthRecord record = healthRecordRepository.save(new HealthRecord(
                UUID.randomUUID(),
                request.patientProfileId(),
                request.doctorProfileId(),
                request.hospitalId(),
                request.branchId(),
                request.claimId(),
                request.visitDate(),
                request.visitType(),
                request.recordType(),
                request.diagnosis(),
                request.notes(),
                HealthRecordStatus.ACTIVE,
                now,
                now
        ));
        return mapper.toResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<HealthRecordResponse> getMyHealthRecords(AuthContext authContext, Pageable pageable) {
        UUID patientProfileId = accessService.currentPatientProfileId(authContext);
        return healthRecordRepository.findByPatientProfileId(patientProfileId, pageable).map(mapper::toResponse);
    }

    @Transactional
    public HealthRecordResponse getHealthRecord(AuthContext authContext, UUID healthRecordId, String reason) {
        HealthRecord record = requireRecord(healthRecordId);
        accessService.requireCanViewRecord(authContext, record);
        logAccess(record.getId(), null, authContext.userId(), firstRole(authContext), reason);
        return mapper.toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getHealthRecordsByClaim(AuthContext authContext, UUID claimId) {
        return healthRecordRepository.findByClaimId(claimId).stream()
                .filter(record -> accessService.canViewRecord(authContext, record))
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public TreatmentResponse addTreatment(AuthContext authContext, UUID healthRecordId, CreateTreatmentRequest request) {
        HealthRecord record = requireRecord(healthRecordId);
        accessService.requireCanViewRecord(authContext, record);
        Instant now = Instant.now();
        Treatment treatment = treatmentRepository.save(new Treatment(
                UUID.randomUUID(),
                healthRecordId,
                request.serviceType(),
                request.treatmentType(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.medications(),
                request.estimatedCost(),
                request.actualCost(),
                now,
                now
        ));
        return mapper.toResponse(treatment);
    }

    @Transactional
    public HealthRecordResponse archiveHealthRecord(AuthContext authContext, UUID healthRecordId) {
        HealthRecord record = requireRecord(healthRecordId);
        accessService.requireCanViewRecord(authContext, record);
        record.archive(Instant.now());
        return mapper.toResponse(healthRecordRepository.save(record));
    }

    HealthRecord requireRecord(UUID healthRecordId) {
        return healthRecordRepository.findById(healthRecordId)
                .orElseThrow(() -> new HealthRecordException("HEALTH_RECORD_NOT_FOUND", "Health record was not found"));
    }

    void logAccess(UUID healthRecordId, UUID documentId, UUID userId, String accessRole, String reason) {
        accessLogRepository.save(new HealthAccessLog(
                UUID.randomUUID(),
                healthRecordId,
                documentId,
                userId,
                accessRole == null ? "UNKNOWN" : accessRole,
                reason == null || reason.isBlank() ? "Access" : reason,
                Instant.now()
        ));
    }

    private String firstRole(AuthContext authContext) {
        return authContext.roles().isEmpty() ? "UNKNOWN" : authContext.roles().getFirst();
    }
}
