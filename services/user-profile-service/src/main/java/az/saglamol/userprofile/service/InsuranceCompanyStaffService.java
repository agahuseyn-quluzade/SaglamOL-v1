package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.events.profile.InsuranceCompanyStaffCreatedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.userprofile.dto.request.CreateInsuranceCompanyStaffRequest;
import az.saglamol.userprofile.dto.response.InsuranceCompanyStaffResponse;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffProfile;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffStatus;
import az.saglamol.userprofile.entity.OutboxEvent;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.mapper.InsuranceCompanyStaffMapper;
import az.saglamol.userprofile.repository.InsuranceCompanyRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyStaffProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InsuranceCompanyStaffService {

    private final InsuranceCompanyRepository companyRepository;
    private final InsuranceCompanyStaffProfileRepository staffRepository;
    private final InsuranceCompanyStaffMapper staffMapper;
    private final InsuranceCompanyAccessService accessService;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public InsuranceCompanyStaffService(
            InsuranceCompanyRepository companyRepository,
            InsuranceCompanyStaffProfileRepository staffRepository,
            InsuranceCompanyStaffMapper staffMapper,
            InsuranceCompanyAccessService accessService,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.companyRepository = companyRepository;
        this.staffRepository = staffRepository;
        this.staffMapper = staffMapper;
        this.accessService = accessService;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public InsuranceCompanyStaffResponse createStaff(
            UUID companyId,
            AuthContext authContext,
            CreateInsuranceCompanyStaffRequest request
    ) {
        requireCompanyExists(companyId);
        accessService.requireStaffManage(companyId, authContext);
        if (staffRepository.existsByIamUserIdAndInsuranceCompanyId(request.iamUserId(), companyId)) {
            throw conflict("Insurance company staff already exists for this user");
        }
        if (request.employeeCode() != null
                && !request.employeeCode().isBlank()
                && staffRepository.existsByInsuranceCompanyIdAndEmployeeCode(companyId, request.employeeCode())) {
            throw conflict("Insurance company employee code already exists");
        }
        Instant now = Instant.now();
        InsuranceCompanyStaffProfile staff = new InsuranceCompanyStaffProfile(
                UUID.randomUUID(),
                request.iamUserId(),
                companyId,
                request.roleType(),
                request.position(),
                request.employeeCode(),
                InsuranceCompanyStaffStatus.ACTIVE,
                now,
                now
        );
        InsuranceCompanyStaffProfile saved = staffRepository.save(staff);
        outboxEventService.saveEvent("InsuranceCompanyStaff", saved.getId(), InsuranceCompanyStaffCreatedEvent.class.getSimpleName(),
                new InsuranceCompanyStaffCreatedEvent(companyId, saved.getId(), saved.getIamUserId(), saved.getRoleType().name(), Instant.now()));
        return staffMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<InsuranceCompanyStaffResponse> getStaffByCompany(
            UUID companyId,
            AuthContext authContext,
            Pageable pageable
    ) {
        requireCompanyExists(companyId);
        accessService.requireStaffView(companyId, authContext);
        return staffRepository.findAllByInsuranceCompanyId(companyId, pageable)
                .map(staffMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InsuranceCompanyStaffResponse getStaffById(UUID companyId, UUID staffId, AuthContext authContext) {
        requireCompanyExists(companyId);
        accessService.requireStaffView(companyId, authContext);
        InsuranceCompanyStaffProfile staff = staffById(companyId, staffId);
        return staffMapper.toResponse(staff);
    }

    @Transactional
    public InsuranceCompanyStaffResponse changeStaffStatus(
            UUID companyId,
            UUID staffId,
            AuthContext authContext,
            InsuranceCompanyStaffStatus newStatus
    ) {
        requireCompanyExists(companyId);
        accessService.requireStaffManage(companyId, authContext);
        InsuranceCompanyStaffProfile staff = staffById(companyId, staffId);
        staff.changeStatus(newStatus, Instant.now());
        return staffMapper.toResponse(staff);
    }

    private void requireCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new UserProfileException("INSURANCE_COMPANY_NOT_FOUND", "Insurance company was not found");
        }
    }

    private InsuranceCompanyStaffProfile staffById(UUID companyId, UUID staffId) {
        InsuranceCompanyStaffProfile staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new UserProfileException("INSURANCE_STAFF_NOT_FOUND", "Insurance company staff was not found"));
        if (!staff.getInsuranceCompanyId().equals(companyId)) {
            throw new UserProfileException("INSURANCE_STAFF_NOT_FOUND", "Insurance company staff was not found");
        }
        return staff;
    }

    private UserProfileException conflict(String message) {
        return new UserProfileException("INSURANCE_STAFF_ALREADY_EXISTS", message);
    }
}
