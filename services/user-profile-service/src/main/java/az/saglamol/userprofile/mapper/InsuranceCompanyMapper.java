package az.saglamol.userprofile.mapper;

import az.saglamol.userprofile.dto.request.AddressRequest;
import az.saglamol.userprofile.dto.response.InsuranceCompanyResponse;
import az.saglamol.userprofile.entity.Address;
import az.saglamol.userprofile.entity.InsuranceCompany;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InsuranceCompanyMapper {

    InsuranceCompanyResponse toResponse(InsuranceCompany company);

    default Address toAddress(AddressRequest request) {
        if (request == null) {
            return null;
        }
        return new Address(request.country(), request.city(), request.district(), request.street(), request.postalCode());
    }
}
