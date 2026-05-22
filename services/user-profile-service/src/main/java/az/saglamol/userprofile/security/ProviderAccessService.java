package az.saglamol.userprofile.security;

import az.saglamol.common.security.RoleConstants;
import az.saglamol.userprofile.exception.UserProfileException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProviderAccessService {

    public void requireHospitalWrite(String rolesHeader) {
        Set<String> roles = roles(rolesHeader);
        if (!(roles.contains(RoleConstants.ADMIN) || roles.contains(RoleConstants.HOSPITAL_ADMIN))) {
            throw new UserProfileException("FORBIDDEN", "Hospital write permission is required");
        }
    }

    public void requireHospitalRead(String rolesHeader) {
        Set<String> roles = roles(rolesHeader);
        if (!(roles.contains(RoleConstants.ADMIN)
                || roles.contains(RoleConstants.HOSPITAL_ADMIN)
                || roles.contains(RoleConstants.HOSPITAL_STAFF)
                || roles.contains(RoleConstants.DOCTOR)
                || roles.contains(RoleConstants.AGENT))) {
            throw new UserProfileException("FORBIDDEN", "Hospital read permission is required");
        }
    }

    private Set<String> roles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toSet());
    }
}
