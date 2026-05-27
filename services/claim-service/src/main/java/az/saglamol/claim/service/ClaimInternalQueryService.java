package az.saglamol.claim.service;

import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.dto.response.ClaimSummaryResponse;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.claim.mapper.ClaimMapper;
import az.saglamol.claim.repository.ClaimRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClaimInternalQueryService {

    private final ClaimRepository claimRepository;
    private final ClaimMapper mapper;

    public ClaimInternalQueryService(ClaimRepository claimRepository, ClaimMapper mapper) {
        this.claimRepository = claimRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ClaimResponse claim(UUID claimId) {
        return mapper.toResponse(claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimException("CLAIM_NOT_FOUND", "Claim was not found")));
    }

    @Transactional(readOnly = true)
    public ClaimSummaryResponse summary(UUID claimId) {
        return mapper.toSummaryResponse(claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimException("CLAIM_NOT_FOUND", "Claim was not found")));
    }

    @Transactional(readOnly = true)
    public Page<ClaimSummaryResponse> byPatient(UUID patientProfileId, Pageable pageable) {
        return claimRepository.findByPatientProfileId(patientProfileId, pageable).map(mapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimSummaryResponse> byCompany(UUID companyId, Pageable pageable) {
        return claimRepository.findByInsuranceCompanyId(companyId, pageable).map(mapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimSummaryResponse> byHospital(UUID hospitalId, Pageable pageable) {
        return claimRepository.findByHospitalId(hospitalId, pageable).map(mapper::toSummaryResponse);
    }
}
