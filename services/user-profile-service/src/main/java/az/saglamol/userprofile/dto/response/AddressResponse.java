package az.saglamol.userprofile.dto.response;

public record AddressResponse(
        String country,
        String city,
        String district,
        String street,
        String postalCode
) {
}
