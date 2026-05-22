package az.saglamol.common.security;

import java.util.Arrays;

public class RoleChecker {

    public boolean hasRole(String role) {
        return AuthContextHolder.getRequired().hasRole(role);
    }

    public boolean hasAnyRole(String... roles) {
        return AuthContextHolder.getRequired().hasAnyRole(roles);
    }

    public void requireRole(String role) {
        if (!hasRole(role)) {
            throw forbidden("Required role is missing: " + role);
        }
    }

    public void requireAnyRole(String... roles) {
        if (!hasAnyRole(roles)) {
            throw forbidden("One of required roles is missing: " + Arrays.toString(roles));
        }
    }

    public boolean isAdmin() {
        return hasRole(RoleConstants.ADMIN);
    }

    public boolean isPatient() {
        return hasRole(RoleConstants.PATIENT);
    }

    public boolean isDoctor() {
        return hasRole(RoleConstants.DOCTOR);
    }

    public boolean isAgent() {
        return hasRole(RoleConstants.AGENT);
    }

    public boolean isHospitalAdmin() {
        return hasRole(RoleConstants.HOSPITAL_ADMIN);
    }

    public boolean isHospitalStaff() {
        return hasRole(RoleConstants.HOSPITAL_STAFF);
    }

    private InternalAuthException forbidden(String message) {
        return new InternalAuthException("INSUFFICIENT_ROLE", message);
    }
}
