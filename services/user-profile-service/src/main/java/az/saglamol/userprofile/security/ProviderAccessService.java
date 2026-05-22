package az.saglamol.userprofile.security;

import az.saglamol.common.security.RoleConstants;
import az.saglamol.common.security.RoleChecker;
import org.springframework.stereotype.Component;

@Component
public class ProviderAccessService {

    private final RoleChecker roleChecker;

    public ProviderAccessService(RoleChecker roleChecker) {
        this.roleChecker = roleChecker;
    }

    public void requireHospitalWrite() {
        roleChecker.requireAnyRole(RoleConstants.ADMIN, RoleConstants.HOSPITAL_ADMIN);
    }

    public void requireHospitalRead() {
        roleChecker.requireAnyRole(
                RoleConstants.ADMIN,
                RoleConstants.HOSPITAL_ADMIN,
                RoleConstants.HOSPITAL_STAFF,
                RoleConstants.DOCTOR,
                RoleConstants.AGENT
        );
    }
}
