package az.saglamol.userprofile.mapper;

import az.saglamol.userprofile.dto.response.InsuranceCompanyStaffResponse;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InsuranceCompanyStaffMapper {

    InsuranceCompanyStaffResponse toResponse(InsuranceCompanyStaffProfile staffProfile);
}
