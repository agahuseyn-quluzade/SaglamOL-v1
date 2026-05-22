package az.saglamol.userprofile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    @Column(name = "address_country", length = 120)
    private String country;

    @Column(name = "address_city", length = 120)
    private String city;

    @Column(name = "address_district", length = 120)
    private String district;

    @Column(name = "address_street", length = 255)
    private String street;

    @Column(name = "address_postal_code", length = 32)
    private String postalCode;

    protected Address() {
    }

    public Address(String country, String city, String district, String street, String postalCode) {
        this.country = country;
        this.city = city;
        this.district = district;
        this.street = street;
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getStreet() {
        return street;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
