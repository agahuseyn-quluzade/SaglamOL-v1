package az.saglamol.payment.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.payment.client.ProfileScopeClient;
import az.saglamol.payment.client.dto.InsuranceScopeResponse;
import az.saglamol.payment.client.dto.UserProfileSummaryResponse;
import az.saglamol.payment.entity.Invoice;
import az.saglamol.payment.entity.Payment;
import az.saglamol.payment.exception.PaymentException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentAccessService {

    private final ProfileScopeClient profileScopeClient;

    public PaymentAccessService(ProfileScopeClient profileScopeClient) {
        this.profileScopeClient = profileScopeClient;
    }

    public void requireCanCreateCompanyPayment(AuthContext authContext, UUID companyId, UUID patientProfileId) {
        if (isAdmin(authContext)) {
            return;
        }
        if (isPatient(authContext) && patientOwns(authContext, patientProfileId)) {
            return;
        }
        if (hasCompanyAccess(authContext, companyId)) {
            return;
        }
        throw forbidden();
    }

    public void requireCanCreatePayout(AuthContext authContext, UUID companyId, UUID hospitalId) {
        if (isAdmin(authContext) || hasCompanyAccess(authContext, companyId)) {
            return;
        }
        if (hospitalId != null && isHospitalAdmin(authContext) && hospitalOwns(authContext, hospitalId)) {
            return;
        }
        throw forbidden();
    }

    public void requireCanViewPayment(AuthContext authContext, Payment payment) {
        if (isAdmin(authContext)) {
            return;
        }
        if (payment.getPatientProfileId() != null && isPatient(authContext) && patientOwns(authContext, payment.getPatientProfileId())) {
            return;
        }
        if (hasCompanyAccess(authContext, payment.getInsuranceCompanyId())) {
            return;
        }
        if (payment.getHospitalId() != null && isHospitalAdmin(authContext) && hospitalOwns(authContext, payment.getHospitalId())) {
            return;
        }
        throw forbidden();
    }

    public void requireCanViewInvoice(AuthContext authContext, Invoice invoice) {
        if (isAdmin(authContext) || hasCompanyAccess(authContext, invoice.getInsuranceCompanyId())) {
            return;
        }
        if (invoice.getPatientProfileId() != null && isPatient(authContext) && patientOwns(authContext, invoice.getPatientProfileId())) {
            return;
        }
        if (invoice.getHospitalId() != null && isHospitalAdmin(authContext) && hospitalOwns(authContext, invoice.getHospitalId())) {
            return;
        }
        throw forbidden();
    }

    public void requireCanManageInvoice(AuthContext authContext, UUID companyId, UUID hospitalId) {
        if (isAdmin(authContext) || hasCompanyAccess(authContext, companyId)) {
            return;
        }
        if (hospitalId != null && isHospitalAdmin(authContext) && hospitalOwns(authContext, hospitalId)) {
            return;
        }
        throw forbidden();
    }

    public UUID currentPatientProfileId(AuthContext authContext) {
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        return summary == null ? null : summary.patientProfileId();
    }

    public UUID currentHospitalId(AuthContext authContext) {
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        return summary == null ? null : summary.hospitalId();
    }

    public UUID resolveCompanyScope(AuthContext authContext) {
        if (isAdmin(authContext)) {
            return null;
        }
        InsuranceScopeResponse scope = profileScopeClient.insuranceScope(authContext.userId());
        return scope == null ? null : scope.insuranceCompanyId();
    }

    public boolean isAdmin(AuthContext authContext) {
        return authContext.roles().contains(RoleConstants.ADMIN);
    }

    private boolean isPatient(AuthContext authContext) {
        return authContext.roles().contains(RoleConstants.PATIENT);
    }

    private boolean isHospitalAdmin(AuthContext authContext) {
        return authContext.roles().contains(RoleConstants.HOSPITAL_ADMIN);
    }

    private boolean patientOwns(AuthContext authContext, UUID patientProfileId) {
        return patientProfileId != null && patientProfileId.equals(currentPatientProfileId(authContext));
    }

    private boolean hospitalOwns(AuthContext authContext, UUID hospitalId) {
        return hospitalId != null && hospitalId.equals(currentHospitalId(authContext));
    }

    private boolean hasCompanyAccess(AuthContext authContext, UUID companyId) {
        InsuranceScopeResponse scope = profileScopeClient.insuranceScope(authContext.userId());
        return scope != null && companyId != null && companyId.equals(scope.insuranceCompanyId()) && scope.canView();
    }

    private PaymentException forbidden() {
        return new PaymentException("FORBIDDEN", "Current user is not allowed to access this payment resource");
    }
}
