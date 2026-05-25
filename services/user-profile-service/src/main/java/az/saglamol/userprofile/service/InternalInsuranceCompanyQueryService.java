package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.userprofile.dto.response.AccessCheckResponse;
import az.saglamol.userprofile.dto.response.AgentCompanyResponse;
import az.saglamol.userprofile.dto.response.InsuranceScopeResponse;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.InsuranceCompany;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffProfile;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffRoleType;
import az.saglamol.userprofile.entity.InsuranceCompanyStatus;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.mapper.AgentMapper;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyStaffProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InternalInsuranceCompanyQueryService {

    private final InsuranceCompanyRepository companyRepository;
    private final InsuranceCompanyStaffProfileRepository staffRepository;
    private final AgentProfileRepository agentRepository;
    private final InsuranceCompanyAccessService accessService;
    private final AgentMapper agentMapper;

    public InternalInsuranceCompanyQueryService(
            InsuranceCompanyRepository companyRepository,
            InsuranceCompanyStaffProfileRepository staffRepository,
            AgentProfileRepository agentRepository,
            InsuranceCompanyAccessService accessService,
            AgentMapper agentMapper
    ) {
        this.companyRepository = companyRepository;
        this.staffRepository = staffRepository;
        this.agentRepository = agentRepository;
        this.accessService = accessService;
        this.agentMapper = agentMapper;
    }

    @Transactional(readOnly = true)
    public boolean existsActive(UUID companyId) {
        return companyRepository.existsByIdAndStatus(companyId, InsuranceCompanyStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public InsuranceScopeResponse insuranceScope(UUID iamUserId) {
        List<String> roles = new ArrayList<>();
        UUID companyId = null;
        boolean isInsuranceAdmin = false;
        boolean isInsuranceStaff = false;
        boolean isAgent = false;

        var staff = staffRepository.findByIamUserId(iamUserId);
        if (staff.isPresent()) {
            InsuranceCompanyStaffProfile profile = staff.get();
            companyId = profile.getInsuranceCompanyId();
            isInsuranceAdmin = profile.getRoleType() == InsuranceCompanyStaffRoleType.INSURANCE_ADMIN;
            isInsuranceStaff = profile.getRoleType() == InsuranceCompanyStaffRoleType.INSURANCE_STAFF;
            roles.add(profile.getRoleType().name());
        }

        var agent = agentRepository.findByIamUserId(iamUserId);
        if (agent.isPresent()) {
            companyId = agent.get().getInsuranceCompanyId();
            isAgent = true;
            roles.add(RoleConstants.AGENT);
        }

        boolean canView = companyId != null && (isInsuranceAdmin || isInsuranceStaff || isAgent);
        boolean canManage = companyId != null && isInsuranceAdmin;
        return new InsuranceScopeResponse(
                iamUserId,
                companyId,
                roles.stream().distinct().toList(),
                canView,
                canManage,
                isAgent,
                isInsuranceAdmin,
                isInsuranceStaff
        );
    }

    @Transactional(readOnly = true)
    public AgentCompanyResponse agentCompany(UUID agentProfileId) {
        AgentProfile agent = agentRepository.findById(agentProfileId)
                .orElseThrow(() -> new UserProfileException("AGENT_PROFILE_NOT_FOUND", "Agent profile was not found"));
        if (agent.getInsuranceCompanyId() == null) {
            throw new UserProfileException("INSURANCE_COMPANY_NOT_FOUND", "Agent is not linked to an insurance company");
        }
        InsuranceCompany company = companyRepository.findById(agent.getInsuranceCompanyId())
                .orElseThrow(() -> new UserProfileException("INSURANCE_COMPANY_NOT_FOUND", "Insurance company was not found"));
        return agentMapper.toCompanyResponse(agent, company);
    }

    @Transactional(readOnly = true)
    public AccessCheckResponse accessForCurrent(UUID companyId, AuthContext authContext) {
        UUID scope = accessService.resolveCompanyScopeForCurrentUser(authContext);
        return new AccessCheckResponse(
                authContext.userId(),
                companyId,
                scope,
                accessService.canViewCompany(companyId, authContext),
                accessService.canManageCompany(companyId, authContext),
                accessService.canManageStaff(companyId, authContext),
                accessService.canAgentOperateForCompany(authContext.userId(), companyId)
        );
    }
}
