package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffProfile;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffRoleType;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffStatus;
import az.saglamol.userprofile.entity.ProfileStatus;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyStaffProfileRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsuranceCompanyAccessServiceTest {

    private final InsuranceCompanyStaffProfileRepository staffRepository = mock(InsuranceCompanyStaffProfileRepository.class);
    private final AgentProfileRepository agentRepository = mock(AgentProfileRepository.class);
    private final InsuranceCompanyAccessService service = new InsuranceCompanyAccessService(staffRepository, agentRepository);

    @Test
    void adminBypassesCompanyScope() {
        UUID companyId = UUID.randomUUID();
        AuthContext admin = auth(UUID.randomUUID(), RoleConstants.ADMIN);

        assertTrue(service.canViewCompany(companyId, admin));
        assertTrue(service.canManageCompany(companyId, admin));
        assertDoesNotThrow(() -> service.requireAgentOrInsuranceStaffInCompany(admin, companyId));
    }

    @Test
    void insuranceAdminCanManageOwnCompanyOnly() {
        UUID userId = UUID.randomUUID();
        UUID ownCompanyId = UUID.randomUUID();
        UUID anotherCompanyId = UUID.randomUUID();
        when(staffRepository.findByIamUserId(userId)).thenReturn(Optional.of(staff(userId, ownCompanyId)));
        AuthContext admin = auth(userId, RoleConstants.INSURANCE_ADMIN);

        assertTrue(service.canManageCompany(ownCompanyId, admin));
        assertFalse(service.canManageCompany(anotherCompanyId, admin));
        assertThrows(UserProfileException.class, () -> service.requireCanManageCompany(admin, anotherCompanyId));
    }

    @Test
    void agentCanOperateForOwnCompanyOnly() {
        UUID userId = UUID.randomUUID();
        UUID ownCompanyId = UUID.randomUUID();
        UUID anotherCompanyId = UUID.randomUUID();
        when(agentRepository.findByIamUserId(userId)).thenReturn(Optional.of(agent(userId, ownCompanyId)));
        AuthContext agent = auth(userId, RoleConstants.AGENT);

        assertTrue(service.canViewCompany(ownCompanyId, agent));
        assertTrue(service.canAgentOperateForCompany(userId, ownCompanyId));
        assertFalse(service.canAgentOperateForCompany(userId, anotherCompanyId));
        assertThrows(UserProfileException.class, () -> service.requireAgentOrInsuranceStaffInCompany(agent, anotherCompanyId));
    }

    @Test
    void patientForbiddenFromCompanyAccess() {
        AuthContext patient = auth(UUID.randomUUID(), RoleConstants.PATIENT);

        assertFalse(service.canViewCompany(UUID.randomUUID(), patient));
        assertThrows(UserProfileException.class, () -> service.requireCanViewCompany(patient, UUID.randomUUID()));
    }

    private AuthContext auth(UUID userId, String role) {
        return new AuthContext(userId, List.of(role), "corr", Map.of());
    }

    private InsuranceCompanyStaffProfile staff(UUID userId, UUID companyId) {
        Instant now = Instant.now();
        return new InsuranceCompanyStaffProfile(
                UUID.randomUUID(),
                userId,
                companyId,
                InsuranceCompanyStaffRoleType.INSURANCE_ADMIN,
                "Admin",
                "EMP-1",
                InsuranceCompanyStaffStatus.ACTIVE,
                now,
                now
        );
    }

    private AgentProfile agent(UUID userId, UUID companyId) {
        Instant now = Instant.now();
        return new AgentProfile(
                UUID.randomUUID(),
                userId,
                companyId,
                "Agent",
                "One",
                "AG-1",
                "Claims",
                "+994501234567",
                "agent@saglamol.az",
                ProfileStatus.ACTIVE,
                now,
                now
        );
    }
}
