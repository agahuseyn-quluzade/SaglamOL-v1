package az.saglamol.healthrecord.controller;

import az.saglamol.healthrecord.dto.request.DocumentHashBatchRequest;
import az.saglamol.healthrecord.dto.response.MedicalDocumentHashResponse;
import az.saglamol.healthrecord.service.InternalServiceSecretVerifier;
import az.saglamol.healthrecord.service.MedicalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/documents")
@Tag(name = "Internal Documents", description = "Internal document hash APIs")
public class DocumentInternalController {

    private final MedicalDocumentService documentService;
    private final InternalServiceSecretVerifier secretVerifier;

    public DocumentInternalController(
            MedicalDocumentService documentService,
            InternalServiceSecretVerifier secretVerifier
    ) {
        this.documentService = documentService;
        this.secretVerifier = secretVerifier;
    }

    @PostMapping("/hash/batch")
    @Operation(summary = "Resolve document hashes by document IDs")
    @ApiResponse(responseCode = "200", description = "Document hashes returned",
            headers = @Header(name = "X-Internal-Service-Secret", description = "Required internal service secret"))
    public List<MedicalDocumentHashResponse> documentHashes(
            @Valid @RequestBody DocumentHashBatchRequest request,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return documentService.documentHashes(request.documentIds());
    }
}
