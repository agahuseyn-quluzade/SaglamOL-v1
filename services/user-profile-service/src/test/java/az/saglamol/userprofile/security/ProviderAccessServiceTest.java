package az.saglamol.userprofile.security;

import az.saglamol.userprofile.exception.UserProfileException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderAccessServiceTest {

    private final ProviderAccessService service = new ProviderAccessService();

    @Test
    void hospitalAdminCanWriteHospitalData() {
        assertDoesNotThrow(() -> service.requireHospitalWrite("PATIENT,HOSPITAL_ADMIN"));
    }

    @Test
    void patientCannotWriteHospitalData() {
        assertThrows(UserProfileException.class, () -> service.requireHospitalWrite("PATIENT"));
    }
}
