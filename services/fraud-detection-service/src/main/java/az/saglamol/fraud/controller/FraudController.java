package az.saglamol.fraud.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.fraud.dto.FraudAssessmentDetailResponse;
import az.saglamol.fraud.dto.FraudSummaryResponse;
import az.saglamol.fraud.service.FraudAssessmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/fraud")
public class FraudController {

    private final FraudAssessmentService assessmentService;

    public FraudController(FraudAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/claims/{claimId}/check")
    public FraudAssessmentDetailResponse checkClaim(@PathVariable UUID claimId) {
        return assessmentService.checkClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/claims/{claimId}")
    public FraudAssessmentDetailResponse byClaim(@PathVariable UUID claimId) {
        return assessmentService.getByClaim(AuthContextHolder.getRequired(), claimId);
    }

    @GetMapping("/assessments/{assessmentId}")
    public FraudAssessmentDetailResponse byAssessment(@PathVariable UUID assessmentId) {
        return assessmentService.getByAssessmentId(AuthContextHolder.getRequired(), assessmentId);
    }

    @GetMapping("/companies/{companyId}/summary")
    public FraudSummaryResponse companySummary(@PathVariable UUID companyId) {
        return assessmentService.companySummary(AuthContextHolder.getRequired(), companyId);
    }

    @GetMapping("/hospitals/{hospitalId}/summary")
    public FraudSummaryResponse hospitalSummary(@PathVariable UUID hospitalId) {
        return assessmentService.hospitalSummary(AuthContextHolder.getRequired(), hospitalId);
    }
}
