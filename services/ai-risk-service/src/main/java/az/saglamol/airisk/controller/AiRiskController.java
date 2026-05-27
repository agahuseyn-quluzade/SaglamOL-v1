package az.saglamol.airisk.controller;

import az.saglamol.airisk.dto.AiRiskAssessmentResponse;
import az.saglamol.airisk.dto.AiRiskSummaryResponse;
import az.saglamol.airisk.service.AiRiskAssessmentService;
import az.saglamol.common.security.AuthContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ai-risk")
public class AiRiskController {

    private final AiRiskAssessmentService assessmentService;

    public AiRiskController(AiRiskAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/claims/{claimId}/assess")
    public AiRiskAssessmentResponse assessClaim(@PathVariable UUID claimId) {
        return assessmentService.assessClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/claims/{claimId}")
    public AiRiskAssessmentResponse byClaim(@PathVariable UUID claimId) {
        return assessmentService.getByClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/assessments/{assessmentId}")
    public AiRiskAssessmentResponse byAssessment(@PathVariable UUID assessmentId) {
        return assessmentService.getByAssessmentId(AuthContextHolder.getRequired(), assessmentId);
    }

    @GetMapping("/companies/{companyId}/summary")
    public AiRiskSummaryResponse companySummary(@PathVariable UUID companyId) {
        return assessmentService.companySummary(AuthContextHolder.getRequired(), companyId);
    }
}
