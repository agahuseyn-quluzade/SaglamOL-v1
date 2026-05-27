package az.saglamol.healthrecord.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.healthrecord.client.ClaimDocumentScopeClient;
import az.saglamol.healthrecord.client.ProfileScopeClient;
import az.saglamol.healthrecord.client.UserProfileSummaryResponse;
import az.saglamol.healthrecord.dto.request.CreateHealthRecordRequest;
import az.saglamol.healthrecord.entity.HealthAccessLog;
import az.saglamol.healthrecord.entity.HealthRecord;
import az.saglamol.healthrecord.entity.HealthRecordStatus;
import az.saglamol.healthrecord.entity.RecordType;
import az.saglamol.healthrecord.entity.VisitType;
import az.saglamol.healthrecord.exception.HealthRecordException;
import az.saglamol.healthrecord.mapper.HealthRecordMapper;
import az.saglamol.healthrecord.repository.HealthAccessLogRepository;
import az.saglamol.healthrecord.repository.HealthRecordRepository;
import az.saglamol.healthrecord.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthRecordBusinessServiceTest {

    @Mock
    private HealthRecordRepository healthRecordRepository;
    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private HealthAccessLogRepository accessLogRepository;
    @Mock
    private ProfileScopeClient profileScopeClient;
    @Mock
    private ClaimDocumentScopeClient claimScopeClient;

    private HealthRecordService service;
    private UUID userId;
    private UUID patientId;
    private UUID doctorId;
    private UUID hospitalId;

    @BeforeEach
    void setUp() {
        HealthRecordMapper mapper = Mappers.getMapper(HealthRecordMapper.class);
        HealthRecordAccessService accessService = new HealthRecordAccessService(profileScopeClient, claimScopeClient);
        service = new HealthRecordService(healthRecordRepository, treatmentRepository, accessLogRepository, accessService, mapper);
        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
    }

    @Test
    void patientCanCreateAndReadOwnRecordWithAccessLog() {
        when(profileScopeClient.userSummary(userId)).thenReturn(patientSummary());
        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.createHealthRecord(patientAuth(), createRequest(patientId, null, null));
        when(healthRecordRepository.findById(created.id())).thenReturn(Optional.of(record(created.id(), patientId, null, null)));

        var read = service.getHealthRecord(patientAuth(), created.id(), "patient view");

        assertEquals(patientId, read.patientProfileId());
        verify(accessLogRepository).save(any(HealthAccessLog.class));
    }

    @Test
    void getMyHealthRecordsUsesPatientScope() {
        when(profileScopeClient.userSummary(userId)).thenReturn(patientSummary());
        when(healthRecordRepository.findByPatientProfileId(patientId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(record(UUID.randomUUID(), patientId, null, null))));

        var page = service.getMyHealthRecords(patientAuth(), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void hospitalAdminCanReadOwnHospitalRecord() {
        UUID recordId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(hospitalSummary());
        when(healthRecordRepository.findById(recordId)).thenReturn(Optional.of(record(recordId, patientId, hospitalId, null)));

        var response = service.getHealthRecord(hospitalAdminAuth(), recordId, "hospital audit");

        assertEquals(hospitalId, response.hospitalId());
    }

    @Test
    void patientCannotReadOtherPatientRecord() {
        UUID recordId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(patientSummary());
        when(healthRecordRepository.findById(recordId)).thenReturn(Optional.of(record(recordId, UUID.randomUUID(), null, null)));

        HealthRecordException exception = assertThrows(HealthRecordException.class,
                () -> service.getHealthRecord(patientAuth(), recordId, "wrong patient"));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    private CreateHealthRecordRequest createRequest(UUID requestPatientId, UUID requestDoctorId, UUID requestHospitalId) {
        return new CreateHealthRecordRequest(
                requestPatientId,
                requestDoctorId,
                requestHospitalId,
                null,
                null,
                LocalDate.now(),
                VisitType.OUTPATIENT,
                RecordType.CONSULTATION,
                "Diagnosis",
                "Notes"
        );
    }

    private HealthRecord record(UUID recordId, UUID recordPatientId, UUID recordHospitalId, UUID claimId) {
        Instant now = Instant.now();
        return new HealthRecord(recordId, recordPatientId, doctorId, recordHospitalId, null, claimId,
                LocalDate.now(), VisitType.OUTPATIENT, RecordType.CONSULTATION, "Diagnosis", "Notes",
                HealthRecordStatus.ACTIVE, now, now);
    }

    private AuthContext patientAuth() {
        return new AuthContext(userId, List.of(RoleConstants.PATIENT), "corr", Map.of());
    }

    private AuthContext hospitalAdminAuth() {
        return new AuthContext(userId, List.of(RoleConstants.HOSPITAL_ADMIN), "corr", Map.of());
    }

    private UserProfileSummaryResponse patientSummary() {
        return new UserProfileSummaryResponse(userId, patientId, null, null, null,
                null, null, null, true, false, false, false, false);
    }

    private UserProfileSummaryResponse hospitalSummary() {
        return new UserProfileSummaryResponse(userId, null, null, null, null,
                UUID.randomUUID(), hospitalId, null, false, false, false, true, false);
    }
}
