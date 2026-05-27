package az.saglamol.userprofile.service;

import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.userprofile.dto.request.CreateInsuranceCompanyRequest;
import az.saglamol.userprofile.dto.request.CreateInsuranceCompanyStaffRequest;
import az.saglamol.userprofile.dto.response.AgentCompanyResponse;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.InsuranceCompany;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffProfile;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffRoleType;
import az.saglamol.userprofile.entity.InsuranceCompanyStatus;
import az.saglamol.userprofile.entity.OutboxEvent;
import az.saglamol.userprofile.entity.ProfileStatus;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.mapper.AgentMapper;
import az.saglamol.userprofile.mapper.InsuranceCompanyMapper;
import az.saglamol.userprofile.mapper.InsuranceCompanyStaffMapper;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyStaffProfileRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsuranceCompanyServiceTest {

    private final InsuranceCompanyRepository companyRepository = mock(InsuranceCompanyRepository.class);
    private final InsuranceCompanyStaffProfileRepository staffRepository = mock(InsuranceCompanyStaffProfileRepository.class);
    private final AgentProfileRepository agentRepository = mock(AgentProfileRepository.class);
    @SuppressWarnings("unchecked")
    private final OutboxEventService<OutboxEvent> outboxEventService = mock(OutboxEventService.class);
    private final InsuranceCompanyAccessService accessService = new InsuranceCompanyAccessService(staffRepository, agentRepository);
    private final InsuranceCompanyService companyService = new InsuranceCompanyService(
            companyRepository,
            Mappers.getMapper(InsuranceCompanyMapper.class),
            accessService,
            outboxEventService
    );
    private final InsuranceCompanyStaffService staffService = new InsuranceCompanyStaffService(
            companyRepository,
            staffRepository,
            Mappers.getMapper(InsuranceCompanyStaffMapper.class),
            accessService,
            outboxEventService
    );
    private final AgentCompanyService agentCompanyService = new AgentCompanyService(
            agentRepository,
            companyRepository,
            Mappers.getMapper(AgentMapper.class),
            accessService
    );

    @Test
    void adminCreatesCompanyWithPendingStatus() {
        when(companyRepository.save(any(InsuranceCompany.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = companyService.createCompany(admin(), companyRequest("TAX-1", "LIC-1"));

        assertEquals("TAX-1", response.taxId());
        assertEquals(InsuranceCompanyStatus.PENDING, response.status());
    }

    @Test
    void duplicateTaxIdFails() {
        when(companyRepository.existsByTaxId("TAX-1")).thenReturn(true);

        UserProfileException exception = assertThrows(UserProfileException.class,
                () -> companyService.createCompany(admin(), companyRequest("TAX-1", "LIC-1")));

        assertEquals("INSURANCE_COMPANY_ALREADY_EXISTS", exception.getErrorCode());
    }

    @Test
    void duplicateLicenseNumberFails() {
        when(companyRepository.existsByLicenseNumber("LIC-1")).thenReturn(true);

        UserProfileException exception = assertThrows(UserProfileException.class,
                () -> companyService.createCompany(admin(), companyRequest("TAX-1", "LIC-1")));

        assertEquals("INSURANCE_COMPANY_ALREADY_EXISTS", exception.getErrorCode());
    }

    @Test
    void adminActivatesCompany() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(companyId, InsuranceCompanyStatus.PENDING)));

        var response = companyService.changeStatus(companyId, admin(), InsuranceCompanyStatus.ACTIVE);

        assertEquals(InsuranceCompanyStatus.ACTIVE, response.status());
    }

    @Test
    void insuranceAdminSeesOwnCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(companyId, InsuranceCompanyStatus.ACTIVE)));
        when(staffRepository.findByIamUserId(userId)).thenReturn(Optional.of(staff(UUID.randomUUID(), userId, companyId)));

        var response = companyService.getCompany(companyId, auth(userId, RoleConstants.INSURANCE_ADMIN));

        assertEquals(companyId, response.id());
    }

    @Test
    void insuranceAdminCannotAccessAnotherCompany() {
        UUID userId = UUID.randomUUID();
        UUID ownCompanyId = UUID.randomUUID();
        UUID anotherCompanyId = UUID.randomUUID();
        when(companyRepository.findById(anotherCompanyId)).thenReturn(Optional.of(company(anotherCompanyId, InsuranceCompanyStatus.ACTIVE)));
        when(staffRepository.findByIamUserId(userId)).thenReturn(Optional.of(staff(UUID.randomUUID(), userId, ownCompanyId)));

        UserProfileException exception = assertThrows(UserProfileException.class,
                () -> companyService.getCompany(anotherCompanyId, auth(userId, RoleConstants.INSURANCE_ADMIN)));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void insuranceAdminAddsStaffToOwnCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(staffRepository.findByIamUserId(userId)).thenReturn(Optional.of(staff(UUID.randomUUID(), userId, companyId)));
        when(staffRepository.save(any(InsuranceCompanyStaffProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = staffService.createStaff(
                companyId,
                auth(userId, RoleConstants.INSURANCE_ADMIN),
                new CreateInsuranceCompanyStaffRequest(UUID.randomUUID(), InsuranceCompanyStaffRoleType.INSURANCE_STAFF, "Operator", "EMP-1")
        );

        assertEquals(companyId, response.insuranceCompanyId());
        assertEquals("EMP-1", response.employeeCode());
    }

    @Test
    void insuranceStaffCanViewOwnCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(companyId, InsuranceCompanyStatus.ACTIVE)));
        when(staffRepository.findByIamUserId(userId)).thenReturn(Optional.of(staff(
                UUID.randomUUID(),
                userId,
                companyId,
                InsuranceCompanyStaffRoleType.INSURANCE_STAFF
        )));

        var response = companyService.getCompany(companyId, auth(userId, RoleConstants.INSURANCE_STAFF));

        assertEquals(companyId, response.id());
    }

    @Test
    void insuranceStaffCannotManageStaff() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(staffRepository.findByIamUserId(userId)).thenReturn(Optional.of(staff(
                UUID.randomUUID(),
                userId,
                companyId,
                InsuranceCompanyStaffRoleType.INSURANCE_STAFF
        )));

        UserProfileException exception = assertThrows(UserProfileException.class, () -> staffService.createStaff(
                companyId,
                auth(userId, RoleConstants.INSURANCE_STAFF),
                new CreateInsuranceCompanyStaffRequest(UUID.randomUUID(), InsuranceCompanyStaffRoleType.INSURANCE_STAFF, "Operator", "EMP-2")
        ));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void agentLinkedToCompany() {
        UUID companyId = UUID.randomUUID();
        UUID agentProfileId = UUID.randomUUID();
        AgentProfile agent = agent(agentProfileId, UUID.randomUUID(), null);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(companyId, InsuranceCompanyStatus.ACTIVE)));
        when(agentRepository.findById(agentProfileId)).thenReturn(Optional.of(agent));

        AgentCompanyResponse response = agentCompanyService.linkAgentToCompany(agentProfileId, companyId, admin());

        assertEquals(agentProfileId, response.agentProfileId());
        assertEquals(companyId, response.insuranceCompanyId());
        assertEquals(companyId, agent.getInsuranceCompanyId());
    }

    @Test
    void patientForbiddenFromCompanyManagement() {
        UserProfileException exception = assertThrows(UserProfileException.class,
                () -> companyService.createCompany(auth(UUID.randomUUID(), RoleConstants.PATIENT), companyRequest("TAX-1", "LIC-1")));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void hospitalAdminForbiddenFromCompanyManagement() {
        UserProfileException exception = assertThrows(UserProfileException.class,
                () -> companyService.createCompany(auth(UUID.randomUUID(), RoleConstants.HOSPITAL_ADMIN), companyRequest("TAX-1", "LIC-1")));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void agentGetsAgentsByOwnCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        AgentProfile agent = agent(UUID.randomUUID(), userId, companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(companyId, InsuranceCompanyStatus.ACTIVE)));
        when(agentRepository.findByIamUserId(userId)).thenReturn(Optional.of(agent));
        var pageable = PageRequest.of(0, 10);
        when(agentRepository.findByInsuranceCompanyId(eq(companyId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(agent), pageable, 1));

        var page = agentCompanyService.getAgentsByCompany(companyId, auth(userId, RoleConstants.AGENT), pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(companyId, page.getContent().getFirst().insuranceCompanyId());
    }

    @Test
    void agentCannotOperateForAnotherCompany() {
        UUID userId = UUID.randomUUID();
        UUID ownCompanyId = UUID.randomUUID();
        UUID anotherCompanyId = UUID.randomUUID();
        when(companyRepository.findById(anotherCompanyId)).thenReturn(Optional.of(company(anotherCompanyId, InsuranceCompanyStatus.ACTIVE)));
        when(agentRepository.findByIamUserId(userId)).thenReturn(Optional.of(agent(UUID.randomUUID(), userId, ownCompanyId)));

        UserProfileException exception = assertThrows(UserProfileException.class,
                () -> agentCompanyService.getAgentsByCompany(anotherCompanyId, auth(userId, RoleConstants.AGENT), PageRequest.of(0, 10)));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    private CreateInsuranceCompanyRequest companyRequest(String taxId, String licenseNumber) {
        return new CreateInsuranceCompanyRequest("Company", taxId, licenseNumber, "company@saglamol.az", "+994501234567", null);
    }

    private AuthContext admin() {
        return auth(UUID.randomUUID(), RoleConstants.ADMIN);
    }

    private AuthContext auth(UUID userId, String... roles) {
        return new AuthContext(userId, List.of(roles), "corr-1", Map.of());
    }

    private InsuranceCompany company(UUID companyId, InsuranceCompanyStatus status) {
        Instant now = Instant.now();
        return new InsuranceCompany(companyId, "Company", "TAX-1", "LIC-1", "company@saglamol.az", "+994501234567", null, status, now, now);
    }

    private InsuranceCompanyStaffProfile staff(UUID staffId, UUID userId, UUID companyId) {
        return staff(staffId, userId, companyId, InsuranceCompanyStaffRoleType.INSURANCE_ADMIN);
    }

    private InsuranceCompanyStaffProfile staff(UUID staffId, UUID userId, UUID companyId, InsuranceCompanyStaffRoleType roleType) {
        Instant now = Instant.now();
        return new InsuranceCompanyStaffProfile(
                staffId,
                userId,
                companyId,
                roleType,
                "Admin",
                "EMP-ADMIN",
                az.saglamol.userprofile.entity.InsuranceCompanyStaffStatus.ACTIVE,
                now,
                now
        );
    }

    private AgentProfile agent(UUID profileId, UUID userId, UUID companyId) {
        Instant now = Instant.now();
        return new AgentProfile(
                profileId,
                userId,
                companyId,
                "Agent",
                "One",
                "AG-1",
                "Sales",
                "+994501234567",
                "agent@saglamol.az",
                ProfileStatus.ACTIVE,
                now,
                now
        );
    }
}
