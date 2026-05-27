package az.saglamol.healthrecord.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.healthrecord.dto.request.ConfirmDocumentUploadRequest;
import az.saglamol.healthrecord.dto.request.InitiateDocumentUploadRequest;
import az.saglamol.healthrecord.dto.response.InitiateDocumentUploadResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentDownloadResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentResponse;
import az.saglamol.healthrecord.service.MedicalDocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-records/{healthRecordId}/documents")
public class MedicalDocumentController {

    private final MedicalDocumentService documentService;

    public MedicalDocumentController(MedicalDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/uploads")
    public InitiateDocumentUploadResponse initiateUpload(
            @PathVariable UUID healthRecordId,
            @Valid @RequestBody InitiateDocumentUploadRequest request
    ) {
        return documentService.initiateUpload(AuthContextHolder.getRequired(), healthRecordId, request);
    }

    @PutMapping("/{documentId}/confirm")
    public MedicalDocumentResponse confirmUpload(
            @PathVariable UUID documentId,
            @Valid @RequestBody ConfirmDocumentUploadRequest request
    ) {
        return documentService.confirmUpload(AuthContextHolder.getRequired(), documentId, request);
    }

    @GetMapping("/{documentId}")
    public MedicalDocumentDownloadResponse document(
            @PathVariable UUID documentId,
            @RequestParam(required = false) String reason
    ) {
        return documentService.getDocument(AuthContextHolder.getRequired(), documentId, reason);
    }

    @DeleteMapping("/{documentId}")
    public void delete(@PathVariable UUID documentId) {
        documentService.deleteDocument(AuthContextHolder.getRequired(), documentId);
    }
}
