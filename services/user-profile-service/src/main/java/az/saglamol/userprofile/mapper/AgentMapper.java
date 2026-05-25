package az.saglamol.userprofile.mapper;

import az.saglamol.userprofile.dto.response.AgentCompanyResponse;
import az.saglamol.userprofile.dto.response.AgentProfileResponse;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.InsuranceCompany;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentMapper {

    AgentProfileResponse toResponse(AgentProfile profile);

    default AgentCompanyResponse toCompanyResponse(AgentProfile agentProfile, InsuranceCompany company) {
        return new AgentCompanyResponse(agentProfile.getId(), company.getId(), company.getName());
    }
}
