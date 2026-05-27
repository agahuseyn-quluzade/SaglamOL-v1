package az.saglamol.notification.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.notification.entity.Notification;
import az.saglamol.notification.exception.NotificationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class NotificationAccessService {

    public void requireCanView(AuthContext authContext, Notification notification) {
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return;
        }
        if (authContext.hasRole(RoleConstants.PATIENT) && authContext.userId().equals(notification.getRecipientUserId())) {
            return;
        }
        if (authContext.hasRole(RoleConstants.INSURANCE_ADMIN)
                && notification.getInsuranceCompanyId() != null
                && notification.getInsuranceCompanyId().equals(companyId(authContext.rawHeaders()))) {
            return;
        }
        throw new NotificationException("FORBIDDEN", "Notification access is forbidden");
    }

    public void requireCanViewUser(AuthContext authContext, UUID userId) {
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return;
        }
        if (authContext.hasRole(RoleConstants.PATIENT) && authContext.userId().equals(userId)) {
            return;
        }
        throw new NotificationException("FORBIDDEN", "User notifications access is forbidden");
    }

    public UUID scopedCompany(AuthContext authContext, UUID requestedCompanyId) {
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return requestedCompanyId;
        }
        if (authContext.hasRole(RoleConstants.INSURANCE_ADMIN)) {
            UUID ownCompanyId = companyId(authContext.rawHeaders());
            if (requestedCompanyId != null && !requestedCompanyId.equals(ownCompanyId)) {
                throw new NotificationException("FORBIDDEN", "Requested company is outside current scope");
            }
            return ownCompanyId;
        }
        throw new NotificationException("FORBIDDEN", "Company notifications access is forbidden");
    }

    public void requireAdmin(AuthContext authContext) {
        if (!authContext.hasRole(RoleConstants.ADMIN)) {
            throw new NotificationException("FORBIDDEN", "Template management is ADMIN only");
        }
    }

    private UUID companyId(Map<String, String> headers) {
        String value = headers.getOrDefault("insuranceCompanyId",
                headers.getOrDefault("X-Insurance-Company-Id", headers.get("x-insurance-company-id")));
        if (value == null || value.isBlank()) {
            throw new NotificationException("FORBIDDEN", "Insurance company scope is missing");
        }
        return UUID.fromString(value);
    }
}
