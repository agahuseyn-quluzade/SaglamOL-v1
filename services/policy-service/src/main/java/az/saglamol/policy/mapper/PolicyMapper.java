package az.saglamol.policy.mapper;

import az.saglamol.policy.dto.response.CoverageRuleResponse;
import az.saglamol.policy.dto.response.InsuranceProductResponse;
import az.saglamol.policy.dto.response.PolicyLimitReservationResponse;
import az.saglamol.policy.dto.response.PolicyResponse;
import az.saglamol.policy.dto.response.ProviderContractResponse;
import az.saglamol.policy.entity.CoverageRule;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyLimitReservation;
import az.saglamol.policy.entity.ProviderContract;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {
    InsuranceProductResponse toResponse(InsuranceProduct product);

    CoverageRuleResponse toResponse(CoverageRule coverageRule);

    PolicyResponse toResponse(Policy policy);

    ProviderContractResponse toResponse(ProviderContract providerContract);

    PolicyLimitReservationResponse toResponse(PolicyLimitReservation reservation);
}
