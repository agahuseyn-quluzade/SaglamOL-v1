package az.saglamol.policy.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.policy.dto.request.CreateProviderContractRequest;
import az.saglamol.policy.dto.response.ProviderContractResponse;
import az.saglamol.policy.service.ProviderContractService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/provider-contracts")
public class ProviderContractController {

    private final ProviderContractService contractService;

    public ProviderContractController(ProviderContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderContractResponse createContract(@Valid @RequestBody CreateProviderContractRequest request) {
        return contractService.createContract(AuthContextHolder.getRequired(), request);
    }

    @GetMapping("/{contractId}")
    public ProviderContractResponse getContract(@PathVariable UUID contractId) {
        return contractService.getContract(contractId, AuthContextHolder.getRequired());
    }

    @GetMapping("/by-company/{companyId}")
    public List<ProviderContractResponse> getContractsByCompany(@PathVariable UUID companyId) {
        return contractService.getContractsByCompany(companyId, AuthContextHolder.getRequired());
    }

    @GetMapping("/by-hospital/{hospitalId}")
    public List<ProviderContractResponse> getContractsByHospital(@PathVariable UUID hospitalId) {
        return contractService.getContractsByHospital(hospitalId, AuthContextHolder.getRequired());
    }

    @PatchMapping("/{contractId}/terminate")
    public ProviderContractResponse terminateContract(@PathVariable UUID contractId) {
        return contractService.terminateContract(contractId, AuthContextHolder.getRequired());
    }
}
