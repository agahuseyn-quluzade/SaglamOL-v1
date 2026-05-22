package az.saglamol.userprofile.service;

import az.saglamol.userprofile.dto.request.CreateHospitalRequest;
import az.saglamol.userprofile.entity.HospitalStatus;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.HospitalBranchRepository;
import az.saglamol.userprofile.repository.HospitalRepository;
import az.saglamol.userprofile.repository.HospitalStaffProfileRepository;
import az.saglamol.userprofile.security.ProviderAccessService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HospitalServiceTest {

    @Test
    void createHospitalDefaultsToPendingStatus() {
        HospitalRepository hospitalRepository = mock(HospitalRepository.class);
        HospitalService service = new HospitalService(
                hospitalRepository,
                mock(HospitalBranchRepository.class),
                mock(HospitalStaffProfileRepository.class),
                mock(DoctorHospitalAssignmentRepository.class),
                mock(DoctorProfileRepository.class),
                mock(ProviderAccessService.class)
        );

        when(hospitalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createHospital(new CreateHospitalRequest(
                "Saglam Hospital",
                "TAX-123",
                "LIC-123",
                "+994501234567",
                "hospital@saglamol.az"
        ));

        assertEquals("Saglam Hospital", response.name());
        assertEquals(HospitalStatus.PENDING.name(), response.status());
    }
}
