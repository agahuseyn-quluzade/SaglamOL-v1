package az.saglamol.userprofile.mapper;

import az.saglamol.userprofile.dto.request.AddressRequest;
import az.saglamol.userprofile.dto.response.AddressResponse;
import az.saglamol.userprofile.dto.response.AgentProfileResponse;
import az.saglamol.userprofile.dto.response.DoctorProfileResponse;
import az.saglamol.userprofile.dto.response.PatientProfileResponse;
import az.saglamol.userprofile.entity.Address;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.PatientProfile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public Address toAddress(AddressRequest request) {
        if (request == null) {
            return null;
        }
        return new Address(request.country(), request.city(), request.district(), request.street(), request.postalCode());
    }

    public PatientProfileResponse toResponse(PatientProfile profile) {
        return new PatientProfileResponse(
                profile.getId(),
                profile.getIamUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getNationalId(),
                toResponse(profile.getAddress()),
                profile.getEmergencyContactName(),
                profile.getEmergencyContactPhone(),
                profile.getProfileStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public DoctorProfileResponse toResponse(DoctorProfile profile) {
        return new DoctorProfileResponse(
                profile.getId(),
                profile.getIamUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getSpecialty(),
                profile.getLicenseNumber(),
                profile.getPhone(),
                profile.getEmail(),
                toResponse(profile.getAddress()),
                profile.getProfileStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public AgentProfileResponse toResponse(AgentProfile profile) {
        return new AgentProfileResponse(
                profile.getId(),
                profile.getIamUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmployeeCode(),
                profile.getDepartment(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getProfileStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(
                address.getCountry(),
                address.getCity(),
                address.getDistrict(),
                address.getStreet(),
                address.getPostalCode()
        );
    }
}
