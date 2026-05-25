package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffProfile;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyStaffProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class InsuranceCompanyAccessService {

    private final InsuranceCompanyStaffProfileRepository staffProfileRepository;
    private final AgentProfileRepository agentProfileRepository;

    public InsuranceCompanyAccessService(
            InsuranceCompanyStaffProfileRepository staffProfileRepository,
            AgentProfileRepository agentProfileRepository
    ) {
        this.staffProfileRepository = staffProfileRepository;
        this.agentProfileRepository = agentProfileRepository;
    }

    public boolean isAdmin(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.ADMIN);
    }

    public boolean isInsuranceAdmin(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.INSURANCE_ADMIN);
    }

    public boolean isInsuranceStaff(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.INSURANCE_STAFF);
    }

    public boolean isAgent(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.AGENT);
    }

    public boolean canViewCompany(UUID companyId, AuthContext authContext) {
        if (isAdmin(authContext)) {
            return true;
        }
        return currentUserInsuranceCompanyId(authContext)
                .map(companyId::equals)
                .orElse(false);
    }

    public boolean canViewCompany(UUID userId, UUID companyId) {
        return staffCompanyId(userId).or(() -> agentCompanyId(userId))
                .map(companyId::equals)
                .orElse(false);
    }

    public boolean canManageCompany(UUID companyId, AuthContext authContext) {
        if (isAdmin(authContext)) {
            return true;
        }
        return isInsuranceAdmin(authContext)
                && currentUserInsuranceCompanyId(authContext).map(companyId::equals).orElse(false);
    }

    public boolean canManageCompany(UUID userId, UUID companyId) {
        return staffCompanyId(userId).map(companyId::equals).orElse(false);
    }

    public boolean canManageStaff(UUID companyId, AuthContext authContext) {
        return canManageCompany(companyId, authContext);
    }

    public boolean canManageCompanyStaff(UUID userId, UUID companyId) {
        return canManageCompany(userId, companyId);
    }

    public boolean canAgentOperateForCompany(UUID userId, UUID companyId) {
        return agentCompanyId(userId).map(companyId::equals).orElse(false);
    }

    public boolean canViewStaff(UUID companyId, AuthContext authContext) {
        if (isAdmin(authContext)) {
            return true;
        }
        if (!isInsuranceAdmin(authContext) && !isInsuranceStaff(authContext)) {
            return false;
        }
        return currentUserInsuranceCompanyId(authContext).map(companyId::equals).orElse(false);
    }

    public void requireAdmin(AuthContext authContext) {
        if (!isAdmin(authContext)) {
            throw forbidden("Only ADMIN can perform this operation");
        }
    }

    public void requireCompanyView(UUID companyId, AuthContext authContext) {
        requireCanViewCompany(authContext, companyId);
    }

    public void requireCompanyManage(UUID companyId, AuthContext authContext) {
        requireCanManageCompany(authContext, companyId);
    }

    public void requireStaffView(UUID companyId, AuthContext authContext) {
        if (!canViewStaff(companyId, authContext)) {
            throw forbidden("Insurance company staff access is forbidden");
        }
    }

    public void requireStaffManage(UUID companyId, AuthContext authContext) {
        if (!canManageStaff(companyId, authContext)) {
            throw forbidden("Insurance company staff management is forbidden");
        }
    }

    public void requireCanViewCompany(AuthContext authContext, UUID companyId) {
        if (!canViewCompany(companyId, authContext)) {
            throw forbidden("Insurance company access is forbidden");
        }
    }

    public void requireCanManageCompany(AuthContext authContext, UUID companyId) {
        if (!canManageCompany(companyId, authContext)) {
            throw forbidden("Insurance company management is forbidden");
        }
    }

    public void requireAgentOrInsuranceStaffInCompany(AuthContext authContext, UUID companyId) {
        if (isAdmin(authContext)) {
            return;
        }
        boolean eligibleRole = isAgent(authContext) || isInsuranceAdmin(authContext) || isInsuranceStaff(authContext);
        if (!eligibleRole || !currentUserInsuranceCompanyId(authContext).map(companyId::equals).orElse(false)) {
            throw forbidden("Insurance company operation is forbidden");
        }
    }

    public Optional<UUID> ownInsuranceCompanyId(AuthContext authContext) {
        return currentUserInsuranceCompanyId(authContext);
    }

    public UUID getCurrentUserInsuranceCompanyId(AuthContext authContext) {
        return currentUserInsuranceCompanyId(authContext).orElse(null);
    }

    public UUID resolveCompanyScopeForCurrentUser(AuthContext authContext) {
        return getCurrentUserInsuranceCompanyId(authContext);
    }

    private Optional<UUID> currentUserInsuranceCompanyId(AuthContext authContext) {
        if (isAdmin(authContext)) {
            return Optional.empty();
        }
        if (isInsuranceAdmin(authContext) || isInsuranceStaff(authContext)) {
            return staffCompanyId(authContext.userId());
        }
        if (isAgent(authContext)) {
            return agentCompanyId(authContext.userId());
        }
        return Optional.empty();
    }

    private Optional<UUID> staffCompanyId(UUID userId) {
        return staffProfileRepository.findByIamUserId(userId)
                .map(InsuranceCompanyStaffProfile::getInsuranceCompanyId);
    }

    private Optional<UUID> agentCompanyId(UUID userId) {
        return agentProfileRepository.findByIamUserId(userId)
                .map(AgentProfile::getInsuranceCompanyId);
    }

    private UserProfileException forbidden(String message) {
        return new UserProfileException("FORBIDDEN", message);
    }
}
