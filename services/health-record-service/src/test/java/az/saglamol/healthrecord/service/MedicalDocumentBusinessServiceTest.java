package az.saglamol.healthrecord.service;

import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.healthrecord.client.ClaimDocumentScopeClient;
import az.saglamol.healthrecord.client.ProfileScopeClient;
import az.saglamol.healthrecord.client.UserProfileSummaryResponse;
import az.saglamol.healthrecord.dto.request.ConfirmDocumentUploadRequest;
import az.saglamol.healthrecord.dto.request.InitiateDocumentUploadRequest;
import az.saglamol.healthrecord.entity.DocumentHashIndex;
import az.saglamol.healthrecord.entity.DocumentType;
import az.saglamol.healthrecord.entity.HealthAccessLog;
import az.saglamol.healthrecord.entity.HealthRecord;
import az.saglamol.healthrecord.entity.HealthRecordStatus;
import az.saglamol.healthrecord.entity.MedicalDocument;
import az.saglamol.healthrecord.entity.MedicalDocumentStatus;
import az.saglamol.healthrecord.entity.OutboxEvent;
import az.saglamol.healthrecord.entity.RecordType;
import az.saglamol.healthrecord.entity.VisitType;
import az.saglamol.healthrecord.exception.HealthRecordException;
import az.saglamol.healthrecord.mapper.HealthRecordMapper;
import az.saglamol.healthrecord.repository.DocumentHashIndexRepository;
import az.saglamol.healthrecord.repository.HealthAccessLogRepository;
import az.saglamol.healthrecord.repository.HealthRecordRepository;
import az.saglamol.healthrecord.repository.MedicalDocumentRepository;
import az.saglamol.healthrecord.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalDocumentBusinessServiceTest {

    @Mock
    private MedicalDocumentRepository documentRepository;
    @Mock
    private DocumentHashIndexRepository hashIndexRepository;
    @Mock
    private HealthRecordRepository healthRecordRepository;
    @Mock
    private HealthAccessLogRepository accessLogRepository;
    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private ProfileScopeClient profileScopeClient;
    @Mock
    private ClaimDocumentScopeClient claimScopeClient;
    @Mock
    private MinioStorageService storageService;
    @Mock
    private OutboxEventService<OutboxEvent> outboxEventService;

    private MedicalDocumentService service;
    private UUID userId;
    private UUID patientId;
    private UUID hospitalId;
    private UUID claimId;
    private UUID recordId;

    @BeforeEach
    void setUp() {
        HealthRecordMapper mapper = Mappers.getMapper(HealthRecordMapper.class);
        HealthRecordAccessService accessService = new HealthRecordAccessService(profileScopeClient, claimScopeClient);
        HealthRecordService healthRecordService = new HealthRecordService(
                healthRecordRepository,
                treatmentRepository,
                accessLogRepository,
                accessService,
                mapper
        );
        service = new MedicalDocumentService(
                documentRepository,
                hashIndexRepository,
                healthRecordRepository,
                accessService,
                healthRecordService,
                storageService,
                mapper,
                outboxEventService,
                "medical-documents"
        );
        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        claimId = UUID.randomUUID();
        recordId = UUID.randomUUID();
    }

    @Test
    void initiateUploadCreatesPendingDocumentAndReturnsUploadUrl() {
        when(profileScopeClient.userSummary(userId)).thenReturn(hospitalSummary());
        when(healthRecordRepository.findById(recordId)).thenReturn(Optional.of(record()));
        when(documentRepository.save(any(MedicalDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storageService.generatePresignedUploadUrl(any())).thenReturn("http://minio/upload");

        var response = service.initiateUpload(hospitalAuth(), recordId, initiateRequest());

        assertEquals("http://minio/upload", response.uploadUrl());
        assertEquals("medical-documents", response.storageBucket());
        assertTrue(response.minioKey().contains(patientId.toString()));
    }

    @Test
    void confirmUploadSavesHashIndexAndOutboxEvent() {
        UUID documentId = UUID.randomUUID();
        MedicalDocument document = pendingDocument(documentId);
        when(profileScopeClient.userSummary(userId)).thenReturn(hospitalSummary());
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(hashIndexRepository.existsBySha256Hash("d".repeat(64))).thenReturn(false);
        when(documentRepository.save(any(MedicalDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.confirmUpload(hospitalAuth(), documentId, new ConfirmDocumentUploadRequest("d".repeat(64)));

        assertEquals(MedicalDocumentStatus.CONFIRMED, response.status());
        assertEquals("d".repeat(64), response.sha256Hash());
        verify(hashIndexRepository).save(any(DocumentHashIndex.class));
        verify(outboxEventService).saveEvent(eq("MedicalDocument"), eq(documentId), eq("MedicalDocumentConfirmedEvent"), any(Object.class));
    }

    @Test
    void duplicateSha256FailsOnConfirm() {
        UUID documentId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(hospitalSummary());
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(pendingDocument(documentId)));
        when(hashIndexRepository.existsBySha256Hash("e".repeat(64))).thenReturn(true);

        HealthRecordException exception = assertThrows(HealthRecordException.class,
                () -> service.confirmUpload(hospitalAuth(), documentId, new ConfirmDocumentUploadRequest("e".repeat(64))));

        assertEquals("DOCUMENT_HASH_ALREADY_EXISTS", exception.getErrorCode());
    }

    @Test
    void getDocumentReturnsDownloadUrlAndWritesAccessLog() {
        UUID documentId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(patientSummary());
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(confirmedDocument(documentId)));
        when(storageService.generatePresignedDownloadUrl("key.pdf")).thenReturn("http://minio/download");

        var response = service.getDocument(patientAuth(), documentId, "claim review");

        assertEquals("http://minio/download", response.downloadUrl());
        verify(accessLogRepository).save(any(HealthAccessLog.class));
    }

    @Test
    void agentCanReadClaimDocumentsWhenClaimScopeAllows() {
        UUID documentId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(agentSummary());
        when(claimScopeClient.canAccessClaimDocuments(any(), eq(claimId))).thenReturn(true);
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(confirmedDocument(documentId)));
        when(storageService.generatePresignedDownloadUrl("key.pdf")).thenReturn("http://minio/download");

        var response = service.getDocument(agentAuth(), documentId, "claim review");

        assertEquals(claimId, response.metadata().claimId());
    }

    @Test
    void minioFailureBubblesAsBusinessException() {
        when(profileScopeClient.userSummary(userId)).thenReturn(hospitalSummary());
        when(healthRecordRepository.findById(recordId)).thenReturn(Optional.of(record()));
        when(documentRepository.save(any(MedicalDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storageService.generatePresignedUploadUrl(any()))
                .thenThrow(new HealthRecordException("MINIO_OPERATION_FAILED", "Failed"));

        HealthRecordException exception = assertThrows(HealthRecordException.class,
                () -> service.initiateUpload(hospitalAuth(), recordId, initiateRequest()));

        assertEquals("MINIO_OPERATION_FAILED", exception.getErrorCode());
    }

    private InitiateDocumentUploadRequest initiateRequest() {
        return new InitiateDocumentUploadRequest(null, claimId, patientId, hospitalId,
                DocumentType.INVOICE, "invoice.pdf", 100L, "application/pdf");
    }

    private HealthRecord record() {
        Instant now = Instant.now();
        return new HealthRecord(recordId, patientId, UUID.randomUUID(), hospitalId, null, claimId,
                LocalDate.now(), VisitType.OUTPATIENT, RecordType.CONSULTATION, "Diagnosis", "Notes",
                HealthRecordStatus.ACTIVE, now, now);
    }

    private MedicalDocument pendingDocument(UUID documentId) {
        return document(documentId, null, MedicalDocumentStatus.PENDING_UPLOAD);
    }

    private MedicalDocument confirmedDocument(UUID documentId) {
        return document(documentId, "f".repeat(64), MedicalDocumentStatus.CONFIRMED);
    }

    private MedicalDocument document(UUID documentId, String hash, MedicalDocumentStatus status) {
        Instant now = Instant.now();
        return new MedicalDocument(documentId, recordId, null, claimId, patientId, hospitalId, userId,
                DocumentType.INVOICE, "invoice.pdf", 100L, "application/pdf", "medical-documents", "key.pdf",
                hash, status, now, now);
    }

    private AuthContext hospitalAuth() {
        return new AuthContext(userId, List.of(RoleConstants.HOSPITAL_ADMIN), "corr", Map.of());
    }

    private AuthContext patientAuth() {
        return new AuthContext(userId, List.of(RoleConstants.PATIENT), "corr", Map.of());
    }

    private AuthContext agentAuth() {
        return new AuthContext(userId, List.of(RoleConstants.AGENT), "corr", Map.of());
    }

    private UserProfileSummaryResponse hospitalSummary() {
        return new UserProfileSummaryResponse(userId, null, null, null, null, UUID.randomUUID(), hospitalId, null,
                false, false, false, true, false);
    }

    private UserProfileSummaryResponse patientSummary() {
        return new UserProfileSummaryResponse(userId, patientId, null, null, null, null, null, null,
                true, false, false, false, false);
    }

    private UserProfileSummaryResponse agentSummary() {
        return new UserProfileSummaryResponse(userId, null, null, UUID.randomUUID(), UUID.randomUUID(), null, null, null,
                false, false, true, false, false);
    }
}
