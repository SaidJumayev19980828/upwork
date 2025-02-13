package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AddressDTO extends BaseRepresentationObject {

    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;    

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

    @JsonProperty("area_id")
    private Long areaId;

    private Boolean principal;

    private Long subAreaId;


}
