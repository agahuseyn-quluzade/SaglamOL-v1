package az.saglamol.healthrecord.service;

import az.saglamol.common.events.healthrecord.MedicalDocumentConfirmedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.healthrecord.dto.request.ConfirmDocumentUploadRequest;
import az.saglamol.healthrecord.dto.request.InitiateDocumentUploadRequest;
import az.saglamol.healthrecord.dto.response.InitiateDocumentUploadResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentDownloadResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentHashResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentSummaryResponse;
import az.saglamol.healthrecord.entity.DocumentHashIndex;
import az.saglamol.healthrecord.entity.MedicalDocument;
import az.saglamol.healthrecord.entity.MedicalDocumentStatus;
import az.saglamol.healthrecord.entity.OutboxEvent;
import az.saglamol.healthrecord.exception.HealthRecordException;
import az.saglamol.healthrecord.mapper.HealthRecordMapper;
import az.saglamol.healthrecord.repository.DocumentHashIndexRepository;
import az.saglamol.healthrecord.repository.HealthRecordRepository;
import az.saglamol.healthrecord.repository.MedicalDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalDocumentService {

    private final MedicalDocumentRepository documentRepository;
    private final DocumentHashIndexRepository hashIndexRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthRecordAccessService accessService;
    private final HealthRecordService healthRecordService;
    private final MinioStorageService storageService;
    private final HealthRecordMapper mapper;
    private final OutboxEventService<OutboxEvent> outboxEventService;
    private final String bucketName;

    public MedicalDocumentService(
            MedicalDocumentRepository documentRepository,
            DocumentHashIndexRepository hashIndexRepository,
            HealthRecordRepository healthRecordRepository,
            HealthRecordAccessService accessService,
            HealthRecordService healthRecordService,
            MinioStorageService storageService,
            HealthRecordMapper mapper,
            OutboxEventService<OutboxEvent> outboxEventService,
            @Value("${minio.bucket.medical-documents:medical-documents}") String bucketName
    ) {
        this.documentRepository = documentRepository;
        this.hashIndexRepository = hashIndexRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.accessService = accessService;
        this.healthRecordService = healthRecordService;
        this.storageService = storageService;
        this.mapper = mapper;
        this.outboxEventService = outboxEventService;
        this.bucketName = bucketName;
    }

    @Transactional
    public InitiateDocumentUploadResponse initiateUpload(AuthContext authContext, UUID healthRecordId,
                                                         InitiateDocumentUploadRequest request) {
        UUID effectivePatientId = request.patientProfileId();
        UUID effectiveHospitalId = request.hospitalId();
        if (healthRecordId != null) {
            var record = healthRecordRepository.findById(healthRecordId)
                    .orElseThrow(() -> new HealthRecordException("HEALTH_RECORD_NOT_FOUND", "Health record was not found"));
            accessService.requireCanViewRecord(authContext, record);
            effectivePatientId = record.getPatientProfileId();
            effectiveHospitalId = request.hospitalId() == null ? record.getHospitalId() : request.hospitalId();
        }
        accessService.requireCanUploadDocument(authContext, effectivePatientId, effectiveHospitalId, request.claimId());
        UUID documentId = UUID.randomUUID();
        Instant now = Instant.now();
        String objectKey = buildObjectKey(effectivePatientId, documentId, request.fileName());
        MedicalDocument document = documentRepository.save(new MedicalDocument(
                documentId,
                healthRecordId,
                request.treatmentId(),
                request.claimId(),
                effectivePatientId,
                effectiveHospitalId,
                authContext.userId(),
                request.documentType(),
                request.fileName(),
                request.fileSize(),
                request.contentType(),
                bucketName,
                objectKey,
                null,
                MedicalDocumentStatus.PENDING_UPLOAD,
                now,
                now
        ));
        String uploadUrl = storageService.generatePresignedUploadUrl(document.getMinioKey());
        return new InitiateDocumentUploadResponse(
                document.getId(),
                uploadUrl,
                document.getStorageBucket(),
                document.getMinioKey(),
                now.plus(15, ChronoUnit.MINUTES)
        );
    }

    @Transactional
    public MedicalDocumentResponse confirmUpload(AuthContext authContext, UUID documentId, ConfirmDocumentUploadRequest request) {
        MedicalDocument document = requireDocument(documentId);
        accessService.requireCanViewDocument(authContext, document);
        if (hashIndexRepository.existsBySha256Hash(request.sha256Hash())) {
            throw new HealthRecordException("DOCUMENT_HASH_ALREADY_EXISTS", "Document hash already exists");
        }
        Instant now = Instant.now();
        document.confirm(request.sha256Hash(), now);
        MedicalDocument saved = documentRepository.save(document);
        hashIndexRepository.save(new DocumentHashIndex(
                UUID.randomUUID(),
                request.sha256Hash(),
                document.getId(),
                document.getPatientProfileId(),
                document.getClaimId(),
                document.getHospitalId(),
                now
        ));
        outboxEventService.saveEvent(
                "MedicalDocument",
                document.getId(),
                MedicalDocumentConfirmedEvent.class.getSimpleName(),
                new MedicalDocumentConfirmedEvent(
                        document.getId(),
                        document.getHealthRecordId(),
                        document.getPatientProfileId(),
                        document.getSha256Hash(),
                        now
                )
        );
        return mapper.toResponse(saved);
    }

    @Transactional
    public MedicalDocumentDownloadResponse getDocument(AuthContext authContext, UUID documentId, String reason) {
        MedicalDocument document = requireDocument(documentId);
        accessService.requireCanViewDocument(authContext, document);
        String downloadUrl = storageService.generatePresignedDownloadUrl(document.getMinioKey());
        healthRecordService.logAccess(document.getHealthRecordId(), document.getId(), authContext.userId(),
                authContext.roles().isEmpty() ? "UNKNOWN" : authContext.roles().getFirst(), reason);
        return new MedicalDocumentDownloadResponse(mapper.toResponse(document), downloadUrl);
    }

    @Transactional
    public void deleteDocument(AuthContext authContext, UUID documentId) {
        MedicalDocument document = requireDocument(documentId);
        accessService.requireCanViewDocument(authContext, document);
        storageService.deleteObject(document.getMinioKey());
        document.softDelete(Instant.now());
        documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public MedicalDocumentHashResponse documentHash(UUID documentId) {
        MedicalDocument document = requireDocument(documentId);
        return toHashResponse(document);
    }

    @Transactional(readOnly = true)
    public List<MedicalDocumentHashResponse> documentHashes(List<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return List.of();
        }
        return documentRepository.findAllById(documentIds).stream()
                .map(this::toHashResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicalDocumentSummaryResponse documentSummary(UUID documentId) {
        return mapper.toSummaryResponse(requireDocument(documentId));
    }

    @Transactional(readOnly = true)
    public List<MedicalDocumentSummaryResponse> documentsByClaim(UUID claimId) {
        return documentRepository.findByClaimId(claimId).stream()
                .map(mapper::toSummaryResponse)
                .toList();
    }

    MedicalDocument requireDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new HealthRecordException("MEDICAL_DOCUMENT_NOT_FOUND", "Medical document was not found"));
    }

    private String buildObjectKey(UUID patientProfileId, UUID documentId, String fileName) {
        String safeName = fileName == null ? "document" : fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        return "patients/%s/documents/%s/%s".formatted(patientProfileId, documentId, safeName);
    }

    private MedicalDocumentHashResponse toHashResponse(MedicalDocument document) {
        return new MedicalDocumentHashResponse(
                document.getId(),
                document.getSha256Hash(),
                document.getPatientProfileId(),
                document.getClaimId(),
                document.getHospitalId()
        );
    }
}
