package az.saglamol.healthrecord.controller;

import az.saglamol.healthrecord.dto.response.MedicalDocumentHashResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentSummaryResponse;
import az.saglamol.healthrecord.service.InternalServiceSecretVerifier;
import az.saglamol.healthrecord.service.MedicalDocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/health-records")
public class HealthRecordInternalController {

    private final MedicalDocumentService documentService;
    private final InternalServiceSecretVerifier secretVerifier;

    public HealthRecordInternalController(
            MedicalDocumentService documentService,
            InternalServiceSecretVerifier secretVerifier
    ) {
        this.documentService = documentService;
        this.secretVerifier = secretVerifier;
    }

    @GetMapping("/documents/{documentId}/hash")
    public MedicalDocumentHashResponse documentHash(
            @PathVariable UUID documentId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return documentService.documentHash(documentId);
    }

    @GetMapping("/documents/by-claim/{claimId}")
    public List<MedicalDocumentSummaryResponse> documentsByClaim(
            @PathVariable UUID claimId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return documentService.documentsByClaim(claimId);
    }

    @GetMapping("/documents/{documentId}/summary")
    public MedicalDocumentSummaryResponse documentSummary(
            @PathVariable UUID documentId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return documentService.documentSummary(documentId);
    }
}
