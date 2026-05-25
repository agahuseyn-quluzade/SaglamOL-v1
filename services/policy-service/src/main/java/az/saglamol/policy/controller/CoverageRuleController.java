package az.saglamol.policy.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.policy.dto.request.CreateCoverageRuleRequest;
import az.saglamol.policy.dto.request.UpdateCoverageRuleRequest;
import az.saglamol.policy.dto.response.CoverageRuleResponse;
import az.saglamol.policy.entity.RuleStatus;
import az.saglamol.policy.service.CoverageRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insurance-products/{productId}/coverage-rules")
public class CoverageRuleController {

    private final CoverageRuleService coverageRuleService;

    public CoverageRuleController(CoverageRuleService coverageRuleService) {
        this.coverageRuleService = coverageRuleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoverageRuleResponse addCoverageRule(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateCoverageRuleRequest request
    ) {
        return coverageRuleService.addCoverageRule(AuthContextHolder.getRequired(), productId, request);
    }

    @PutMapping("/{ruleId}")
    public CoverageRuleResponse updateCoverageRule(
            @PathVariable UUID productId,
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateCoverageRuleRequest request
    ) {
        return coverageRuleService.updateCoverageRule(AuthContextHolder.getRequired(), ruleId, request);
    }

    @PatchMapping("/{ruleId}/status")
    public CoverageRuleResponse changeStatus(
            @PathVariable UUID productId,
            @PathVariable UUID ruleId,
            @RequestParam RuleStatus status
    ) {
        return coverageRuleService.changeStatus(AuthContextHolder.getRequired(), ruleId, status);
    }

    @GetMapping
    public List<CoverageRuleResponse> getRulesByProduct(@PathVariable UUID productId) {
        return coverageRuleService.getRulesByProduct(productId, AuthContextHolder.getRequired());
    }
}
