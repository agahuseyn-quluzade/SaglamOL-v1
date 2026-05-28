package az.saglamol.fraud.client;

import az.saglamol.fraud.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "health-record-service", contextId = "fraudHealthRecordInternalClient", configuration = FeignInternalClientConfig.class)
public interface HealthRecordInternalClient {
    @GetMapping("/internal/v1/health-records/documents/by-claim/{claimId}")
    List<MedicalDocumentSummaryResponse> documentsByClaim(@PathVariable("claimId") UUID claimId);

    @GetMapping("/internal/v1/health-records/documents/{documentId}/hash")
    MedicalDocumentHashResponse documentHash(@PathVariable("documentId") UUID documentId);

    @PostMapping("/internal/v1/documents/hash/batch")
    List<MedicalDocumentHashResponse> documentHashes(@RequestBody DocumentHashBatchRequest request);
}
