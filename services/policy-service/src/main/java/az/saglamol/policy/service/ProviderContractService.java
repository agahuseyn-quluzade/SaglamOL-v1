package az.saglamol.policy.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.policy.dto.request.CreateProviderContractRequest;
import az.saglamol.policy.dto.response.ProviderContractResponse;
import az.saglamol.policy.entity.ProviderContract;
import az.saglamol.policy.entity.ProviderContractStatus;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.InsuranceProductRepository;
import az.saglamol.policy.repository.ProviderContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProviderContractService {

    private final ProviderContractRepository contractRepository;
    private final InsuranceProductRepository productRepository;
    private final PolicyMapper policyMapper;
    private final PolicyAccessService accessService;

    public ProviderContractService(
            ProviderContractRepository contractRepository,
            InsuranceProductRepository productRepository,
            PolicyMapper policyMapper,
            PolicyAccessService accessService
    ) {
        this.contractRepository = contractRepository;
        this.productRepository = productRepository;
        this.policyMapper = policyMapper;
        this.accessService = accessService;
    }

    @Transactional
    public ProviderContractResponse createContract(AuthContext authContext, CreateProviderContractRequest request) {
        accessService.requireCanManageCompany(authContext, request.insuranceCompanyId());
        if (contractRepository.existsByInsuranceCompanyIdAndContractNumber(request.insuranceCompanyId(), request.contractNumber())) {
            throw new PolicyException("PROVIDER_CONTRACT_ALREADY_EXISTS", "Contract number already exists for insurance company");
        }
        if (request.productId() != null) {
            var product = productRepository.findById(request.productId())
                    .orElseThrow(() -> new PolicyException("PRODUCT_NOT_FOUND", "Insurance product was not found"));
            if (!request.insuranceCompanyId().equals(product.getInsuranceCompanyId())) {
                throw new PolicyException("FORBIDDEN", "Contract product is outside insurance company scope");
            }
        }
        Instant now = Instant.now();
        ProviderContract contract = new ProviderContract(
                UUID.randomUUID(),
                request.insuranceCompanyId(),
                request.hospitalId(),
                request.productId(),
                request.contractNumber(),
                request.startDate(),
                request.endDate(),
                ProviderContractStatus.ACTIVE,
                request.payoutModel(),
                now,
                now
        );
        return policyMapper.toResponse(contractRepository.save(contract));
    }

    @Transactional(readOnly = true)
    public ProviderContractResponse getContract(UUID contractId, AuthContext authContext) {
        ProviderContract contract = contractById(contractId);
        accessService.requireCanViewContract(authContext, contract);
        return policyMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public List<ProviderContractResponse> getContractsByCompany(UUID companyId, AuthContext authContext) {
        accessService.requireCanViewCompany(authContext, companyId);
        return contractRepository.findByInsuranceCompanyId(companyId).stream()
                .map(policyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProviderContractResponse> getContractsByHospital(UUID hospitalId, AuthContext authContext) {
        return contractRepository.findByHospitalId(hospitalId).stream()
                .filter(contract -> accessService.canViewContract(authContext, contract))
                .map(policyMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProviderContractResponse terminateContract(UUID contractId, AuthContext authContext) {
        ProviderContract contract = contractById(contractId);
        accessService.requireCanManageContract(authContext, contract);
        contract.terminate(Instant.now());
        return policyMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public boolean isInNetwork(UUID companyId, UUID hospitalId, UUID productId) {
        return contractRepository.findByInsuranceCompanyId(companyId).stream()
                .filter(contract -> contract.getStatus() == ProviderContractStatus.ACTIVE)
                .filter(contract -> contract.getHospitalId().equals(hospitalId))
                .anyMatch(contract -> contract.getProductId() == null || contract.getProductId().equals(productId));
    }

    private ProviderContract contractById(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new PolicyException("PROVIDER_CONTRACT_NOT_FOUND", "Provider contract was not found"));
    }
}
