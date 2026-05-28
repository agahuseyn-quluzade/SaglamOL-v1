package az.saglamol.healthrecord.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.healthrecord.dto.request.CreateHealthRecordRequest;
import az.saglamol.healthrecord.dto.request.CreateTreatmentRequest;
import az.saglamol.healthrecord.dto.response.HealthRecordResponse;
import az.saglamol.healthrecord.dto.response.TreatmentResponse;
import az.saglamol.healthrecord.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-records")
@Tag(name = "Health Records", description = "Health record and treatment management")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful health record operation")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @PostMapping
    @Operation(summary = "Create health record")
    public HealthRecordResponse create(@Valid @RequestBody CreateHealthRecordRequest request) {
        return healthRecordService.createHealthRecord(AuthContextHolder.getRequired(), request);
    }

    @GetMapping("/my")
    @Operation(summary = "Get current patient's health records")
    public Page<HealthRecordResponse> myRecords(Pageable pageable) {
        return healthRecordService.getMyHealthRecords(AuthContextHolder.getRequired(), pageable);
    }

    @GetMapping("/{healthRecordId}")
    @Operation(summary = "Get health record and write access log")
    public HealthRecordResponse record(
            @PathVariable UUID healthRecordId,
            @RequestParam(required = false) String reason
    ) {
        return healthRecordService.getHealthRecord(AuthContextHolder.getRequired(), healthRecordId, reason);
    }

    @GetMapping("/by-claim/{claimId}")
    @Operation(summary = "Get health records by claim")
    public List<HealthRecordResponse> byClaim(@PathVariable UUID claimId) {
        return healthRecordService.getHealthRecordsByClaim(AuthContextHolder.getRequired(), claimId);
    }

    @PostMapping("/{healthRecordId}/treatments")
    @Operation(summary = "Add treatment to health record")
    public TreatmentResponse addTreatment(
            @PathVariable UUID healthRecordId,
            @Valid @RequestBody CreateTreatmentRequest request
    ) {
        return healthRecordService.addTreatment(AuthContextHolder.getRequired(), healthRecordId, request);
    }

    @PatchMapping("/{healthRecordId}/archive")
    @Operation(summary = "Archive health record")
    public HealthRecordResponse archive(@PathVariable UUID healthRecordId) {
        return healthRecordService.archiveHealthRecord(AuthContextHolder.getRequired(), healthRecordId);
    }
}
