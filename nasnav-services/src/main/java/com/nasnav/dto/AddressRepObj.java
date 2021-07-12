package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class AddressRepObj extends BaseRepresentationObject {

    private Long id;
    private String firstName;
    private String lastName;
    private String flatNumber;
    private String buildingNumber;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    private String addressLine2;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String postalCode;
    private String phoneNumber;
    private Boolean principal;
    private Long areaId;
    private String area;
    private String city;
    private String country;
    private Long subAreaId;
    private String subArea;
}
