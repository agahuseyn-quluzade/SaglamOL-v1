package az.saglamol.healthrecord.controller;

import az.saglamol.healthrecord.dto.response.MedicalDocumentHashResponse;
import az.saglamol.healthrecord.service.InternalServiceSecretVerifier;
import az.saglamol.healthrecord.service.MedicalDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthRecordInternalControllerTest {

    private final MedicalDocumentService documentService = mock(MedicalDocumentService.class);
    private final HealthRecordInternalController controller = new HealthRecordInternalController(
            documentService,
            new InternalServiceSecretVerifier("secret")
    );

    @Test
    void documentHashReturnsWithValidSecret() {
        UUID documentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        when(documentService.documentHash(documentId))
                .thenReturn(new MedicalDocumentHashResponse(documentId, "a".repeat(64), patientId, claimId, hospitalId));

        var response = controller.documentHash(documentId, "secret");

        assertEquals(documentId, response.documentId());
        assertEquals(patientId, response.patientProfileId());
        assertEquals(claimId, response.claimId());
        assertEquals(hospitalId, response.hospitalId());
    }

    @Test
    void documentHashRejectsInvalidSecret() {
        UUID documentId = UUID.randomUUID();

        assertThrows(ResponseStatusException.class, () -> controller.documentHash(documentId, "wrong"));
    }
}
