package az.saglamol.healthrecord.dto.response;

public record MedicalDocumentDownloadResponse(
        MedicalDocumentResponse metadata,
        String downloadUrl
) {
}
