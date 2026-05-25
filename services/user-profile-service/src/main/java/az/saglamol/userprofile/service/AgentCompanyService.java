package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.userprofile.dto.response.AgentCompanyResponse;
import az.saglamol.userprofile.dto.response.AgentProfileResponse;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.InsuranceCompany;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.mapper.AgentMapper;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AgentCompanyService {

    private final AgentProfileRepository agentRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final AgentMapper agentMapper;
    private final InsuranceCompanyAccessService accessService;

    public AgentCompanyService(
            AgentProfileRepository agentRepository,
            InsuranceCompanyRepository companyRepository,
            AgentMapper agentMapper,
            InsuranceCompanyAccessService accessService
    ) {
        this.agentRepository = agentRepository;
        this.companyRepository = companyRepository;
        this.agentMapper = agentMapper;
        this.accessService = accessService;
    }

    @Transactional
    public AgentCompanyResponse linkAgentToCompany(UUID agentProfileId, UUID companyId, AuthContext authContext) {
        InsuranceCompany company = companyById(companyId);
        accessService.requireCompanyManage(companyId, authContext);
        AgentProfile agent = agentRepository.findById(agentProfileId)
                .orElseThrow(() -> new UserProfileException("AGENT_PROFILE_NOT_FOUND", "Agent profile was not found"));
        if (agent.getInsuranceCompanyId() != null && !agent.getInsuranceCompanyId().equals(companyId)) {
            throw new UserProfileException("AGENT_ALREADY_LINKED", "Agent is already linked to another insurance company");
        }
        agent.linkToCompany(companyId, Instant.now());
        return agentMapper.toCompanyResponse(agent, company);
    }

    @Transactional(readOnly = true)
    public Page<AgentProfileResponse> getAgentsByCompany(UUID companyId, AuthContext authContext, Pageable pageable) {
        companyById(companyId);
        accessService.requireCompanyView(companyId, authContext);
        return agentRepository.findByInsuranceCompanyId(companyId, pageable)
                .map(agentMapper::toResponse);
    }

    private InsuranceCompany companyById(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new UserProfileException("INSURANCE_COMPANY_NOT_FOUND", "Insurance company was not found"));
    }
}
