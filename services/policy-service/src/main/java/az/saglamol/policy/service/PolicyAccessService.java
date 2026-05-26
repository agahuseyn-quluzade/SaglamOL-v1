package az.saglamol.policy.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.policy.client.ProfileScopeClient;
import az.saglamol.policy.client.dto.InsuranceScopeResponse;
import az.saglamol.policy.client.dto.UserProfileSummaryResponse;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.entity.ProviderContract;
import az.saglamol.policy.exception.PolicyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PolicyAccessService {

    private final ProfileScopeClient profileScopeClient;

    public PolicyAccessService(ProfileScopeClient profileScopeClient) {
        this.profileScopeClient = profileScopeClient;
    }

    public void requireCanCreateProduct(AuthContext authContext, UUID companyId) {
        requireCanManageCompany(authContext, companyId);
    }

    public void requireCanManageProduct(AuthContext authContext, InsuranceProduct product) {
        requireCanManageCompany(authContext, product.getInsuranceCompanyId());
    }

    public void requireCanViewProduct(AuthContext authContext, InsuranceProduct product) {
        if (canViewProduct(authContext, product)) {
            return;
        }
        throw forbidden("Product access is forbidden");
    }

    public void requireCanManageCompany(AuthContext authContext, UUID companyId) {
        if (isAdmin(authContext)) {
            return;
        }
        InsuranceScopeResponse scope = insuranceScope(authContext);
        if (scope != null && scope.canManage() && companyId.equals(scope.insuranceCompanyId())) {
            return;
        }
        throw forbidden("Insurance company management permission is required");
    }

    public void requireCanOperateForCompany(AuthContext authContext, UUID companyId) {
        if (isAdmin(authContext)) {
            return;
        }
        InsuranceScopeResponse scope = insuranceScope(authContext);
        if (scope != null && scope.canView() && companyId.equals(scope.insuranceCompanyId())
                && (scope.canManage() || scope.isAgent())) {
            return;
        }
        throw forbidden("Insurance company operation permission is required");
    }

    public void requireCanViewCompany(AuthContext authContext, UUID companyId) {
        if (isAdmin(authContext)) {
            return;
        }
        InsuranceScopeResponse scope = insuranceScope(authContext);
        if (scope != null && scope.canView() && companyId.equals(scope.insuranceCompanyId())) {
            return;
        }
        throw forbidden("Insurance company view permission is required");
    }

    public boolean canViewProduct(AuthContext authContext, InsuranceProduct product) {
        if (isAdmin(authContext)) {
            return true;
        }
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            return product.getStatus() == InsuranceProductStatus.ACTIVE;
        }
        InsuranceScopeResponse scope = insuranceScope(authContext);
        if (scope == null || !product.getInsuranceCompanyId().equals(scope.insuranceCompanyId())) {
            return false;
        }
        if (scope.isAgent()) {
            return product.getStatus() == InsuranceProductStatus.ACTIVE;
        }
        return scope.canView();
    }

    public UUID resolveSearchCompany(AuthContext authContext, UUID requestedCompanyId) {
        if (isAdmin(authContext) || authContext.hasRole(RoleConstants.PATIENT)) {
            return requestedCompanyId;
        }
        InsuranceScopeResponse scope = insuranceScope(authContext);
        if (scope == null || scope.insuranceCompanyId() == null || !scope.canView()) {
            throw forbidden("Insurance company scope is required");
        }
        if (requestedCompanyId != null && !requestedCompanyId.equals(scope.insuranceCompanyId())) {
            throw forbidden("Requested company is outside current user scope");
        }
        return scope.insuranceCompanyId();
    }

    public boolean canViewContract(AuthContext authContext, ProviderContract contract) {
        if (isAdmin(authContext)) {
            return true;
        }
        InsuranceScopeResponse scope = insuranceScope(authContext);
        if (scope != null && scope.canView() && contract.getInsuranceCompanyId().equals(scope.insuranceCompanyId())) {
            return true;
        }
        if (authContext.hasRole(RoleConstants.HOSPITAL_ADMIN)) {
            UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
            return summary != null && contract.getHospitalId().equals(summary.hospitalId());
        }
        return false;
    }

    public void requireCanViewContract(AuthContext authContext, ProviderContract contract) {
        if (!canViewContract(authContext, contract)) {
            throw forbidden("Provider contract access is forbidden");
        }
    }

    public void requireCanManageContract(AuthContext authContext, ProviderContract contract) {
        requireCanManageCompany(authContext, contract.getInsuranceCompanyId());
    }

    public boolean canViewPolicy(AuthContext authContext, UUID companyId, UUID patientProfileId) {
        if (isAdmin(authContext)) {
            return true;
        }
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
            return summary != null && patientProfileId.equals(summary.patientProfileId());
        }
        InsuranceScopeResponse scope = insuranceScope(authContext);
        return scope != null && scope.canView() && companyId.equals(scope.insuranceCompanyId());
    }

    public void requireCanViewPolicy(AuthContext authContext, UUID companyId, UUID patientProfileId) {
        if (!canViewPolicy(authContext, companyId, patientProfileId)) {
            throw forbidden("Policy access is forbidden");
        }
    }

    public UUID currentPatientProfileId(AuthContext authContext) {
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        return summary == null ? null : summary.patientProfileId();
    }

    private InsuranceScopeResponse insuranceScope(AuthContext authContext) {
        if (authContext.hasAnyRole(RoleConstants.INSURANCE_ADMIN, RoleConstants.INSURANCE_STAFF, RoleConstants.AGENT)) {
            return profileScopeClient.insuranceScope(authContext.userId());
        }
        return null;
    }

    private boolean isAdmin(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.ADMIN);
    }

    private PolicyException forbidden(String message) {
        return new PolicyException("FORBIDDEN", message);
    }
}
