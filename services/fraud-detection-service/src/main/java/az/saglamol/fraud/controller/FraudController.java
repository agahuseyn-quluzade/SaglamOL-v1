package az.saglamol.fraud.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.fraud.dto.FraudAssessmentDetailResponse;
import az.saglamol.fraud.dto.FraudSummaryResponse;
import az.saglamol.fraud.service.FraudAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/fraud")
@Tag(name = "Fraud Detection", description = "Fraud assessment and scoring endpoints")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful fraud operation")
public class FraudController {

    private final FraudAssessmentService assessmentService;

    public FraudController(FraudAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/claims/{claimId}/check")
    @Operation(summary = "Run fraud check for claim")
    public FraudAssessmentDetailResponse checkClaim(@PathVariable UUID claimId) {
        return assessmentService.checkClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/claims/{claimId}")
    @Operation(summary = "Get latest fraud assessment by claim")
    public FraudAssessmentDetailResponse byClaim(@PathVariable UUID claimId) {
        return assessmentService.getByClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/assessments/{assessmentId}")
    @Operation(summary = "Get fraud assessment by ID")
    public FraudAssessmentDetailResponse byAssessment(@PathVariable UUID assessmentId) {
        return assessmentService.getByAssessmentId(AuthContextHolder.getRequired(), assessmentId);
    }

    @GetMapping("/companies/{companyId}/summary")
    @Operation(summary = "Get company fraud summary")
    public FraudSummaryResponse companySummary(@PathVariable UUID companyId) {
        return assessmentService.companySummary(AuthContextHolder.getRequired(), companyId);
    }

    @GetMapping("/hospitals/{hospitalId}/summary")
    @Operation(summary = "Get hospital fraud summary")
    public FraudSummaryResponse hospitalSummary(@PathVariable UUID hospitalId) {
        return assessmentService.hospitalSummary(AuthContextHolder.getRequired(), hospitalId);
    }
}
