package az.saglamol.airisk.controller;

import az.saglamol.airisk.dto.AiRiskAssessmentResponse;
import az.saglamol.airisk.dto.AiRiskSummaryResponse;
import az.saglamol.airisk.service.AiRiskAssessmentService;
import az.saglamol.common.security.AuthContextHolder;
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
@RequestMapping("/ai-risk")
@Tag(name = "AI Risk", description = "External AI risk assessment endpoints")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful AI risk operation")
public class AiRiskController {

    private final AiRiskAssessmentService assessmentService;

    public AiRiskController(AiRiskAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/claims/{claimId}/assess")
    @Operation(summary = "Run AI risk assessment for claim")
    public AiRiskAssessmentResponse assessClaim(@PathVariable UUID claimId) {
        return assessmentService.assessClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/claims/{claimId}")
    @Operation(summary = "Get latest AI risk assessment by claim")
    public AiRiskAssessmentResponse byClaim(@PathVariable UUID claimId) {
        return assessmentService.getByClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/assessments/{assessmentId}")
    @Operation(summary = "Get AI risk assessment by ID")
    public AiRiskAssessmentResponse byAssessment(@PathVariable UUID assessmentId) {
        return assessmentService.getByAssessmentId(AuthContextHolder.getRequired(), assessmentId);
    }

    @GetMapping("/companies/{companyId}/summary")
    @Operation(summary = "Get company AI risk summary")
    public AiRiskSummaryResponse companySummary(@PathVariable UUID companyId) {
        return assessmentService.companySummary(AuthContextHolder.getRequired(), companyId);
    }
}
