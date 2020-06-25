package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=false)
public class AddressRepObj extends BaseRepresentationObject {

    private Long id;

    @JsonProperty("flat_number")
    private String flatNumber;

    @JsonProperty("building_number")
    private String buildingNumber;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    private String addressLine2;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String area;
    private String city;
    private String country;
}
