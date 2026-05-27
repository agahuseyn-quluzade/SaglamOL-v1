package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.events.profile.InsuranceCompanyActivatedEvent;
import az.saglamol.common.events.profile.InsuranceCompanyCreatedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.userprofile.dto.request.CreateInsuranceCompanyRequest;
import az.saglamol.userprofile.dto.request.UpdateInsuranceCompanyRequest;
import az.saglamol.userprofile.dto.response.InsuranceCompanyResponse;
import az.saglamol.userprofile.entity.InsuranceCompany;
import az.saglamol.userprofile.entity.InsuranceCompanyStatus;
import az.saglamol.userprofile.entity.OutboxEvent;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.mapper.InsuranceCompanyMapper;
import az.saglamol.userprofile.repository.InsuranceCompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InsuranceCompanyService {

    private final InsuranceCompanyRepository companyRepository;
    private final InsuranceCompanyMapper companyMapper;
    private final InsuranceCompanyAccessService accessService;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public InsuranceCompanyService(
            InsuranceCompanyRepository companyRepository,
            InsuranceCompanyMapper companyMapper,
            InsuranceCompanyAccessService accessService,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.accessService = accessService;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public InsuranceCompanyResponse createCompany(AuthContext authContext, CreateInsuranceCompanyRequest request) {
        accessService.requireAdmin(authContext);
        if (companyRepository.existsByTaxId(request.taxId())) {
            throw conflict("Insurance company tax id already exists");
        }
        if (companyRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw conflict("Insurance company license number already exists");
        }
        Instant now = Instant.now();
        InsuranceCompany company = new InsuranceCompany(
                UUID.randomUUID(),
                request.name(),
                request.taxId(),
                request.licenseNumber(),
                request.email(),
                request.phone(),
                companyMapper.toAddress(request.address()),
                InsuranceCompanyStatus.PENDING,
                now,
                now
        );
        InsuranceCompany saved = companyRepository.save(company);
        outboxEventService.saveEvent("InsuranceCompany", saved.getId(), InsuranceCompanyCreatedEvent.class.getSimpleName(),
                new InsuranceCompanyCreatedEvent(saved.getId(), saved.getName(), saved.getTaxId(), Instant.now()));
        return companyMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InsuranceCompanyResponse getCompany(UUID companyId, AuthContext authContext) {
        InsuranceCompany company = companyById(companyId);
        accessService.requireCompanyView(companyId, authContext);
        return companyMapper.toResponse(company);
    }

    @Transactional(readOnly = true)
    public Page<InsuranceCompanyResponse> getAllCompanies(
            InsuranceCompanyStatus status,
            Pageable pageable,
            AuthContext authContext
    ) {
        if (accessService.isAdmin(authContext)) {
            Page<InsuranceCompany> page = status == null
                    ? companyRepository.findAll(pageable)
                    : companyRepository.findByStatus(status, pageable);
            return page.map(companyMapper::toResponse);
        }
        UUID ownCompanyId = accessService.ownInsuranceCompanyId(authContext)
                .orElseThrow(() -> forbidden("Insurance company access is forbidden"));
        InsuranceCompany company = companyById(ownCompanyId);
        if (status != null && company.getStatus() != status) {
            return Page.empty(pageable);
        }
        return new PageImpl<>(List.of(companyMapper.toResponse(company)), pageable, 1);
    }

    @Transactional
    public InsuranceCompanyResponse updateCompany(
            UUID companyId,
            AuthContext authContext,
            UpdateInsuranceCompanyRequest request
    ) {
        InsuranceCompany company = companyById(companyId);
        accessService.requireCompanyManage(companyId, authContext);
        company.update(
                request.name() == null || request.name().isBlank() ? company.getName() : request.name(),
                request.email(),
                request.phone(),
                companyMapper.toAddress(request.address()),
                Instant.now()
        );
        return companyMapper.toResponse(company);
    }

    @Transactional
    public InsuranceCompanyResponse changeStatus(
            UUID companyId,
            AuthContext authContext,
            InsuranceCompanyStatus newStatus
    ) {
        InsuranceCompany company = companyById(companyId);
        accessService.requireCompanyManage(companyId, authContext);
        company.changeStatus(newStatus, Instant.now());
        if (newStatus == InsuranceCompanyStatus.ACTIVE) {
            outboxEventService.saveEvent("InsuranceCompany", company.getId(), InsuranceCompanyActivatedEvent.class.getSimpleName(),
                    new InsuranceCompanyActivatedEvent(company.getId(), Instant.now()));
        }
        return companyMapper.toResponse(company);
    }

    private InsuranceCompany companyById(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new UserProfileException("INSURANCE_COMPANY_NOT_FOUND", "Insurance company was not found"));
    }

    private UserProfileException conflict(String message) {
        return new UserProfileException("INSURANCE_COMPANY_ALREADY_EXISTS", message);
    }

    private UserProfileException forbidden(String message) {
        return new UserProfileException("FORBIDDEN", message);
    }
}
