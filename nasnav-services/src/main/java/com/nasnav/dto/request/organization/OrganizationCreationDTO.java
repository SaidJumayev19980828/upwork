package com.nasnav.dto.request.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.YeshteryState;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganizationCreationDTO {
    private Long id;
    private String name;
    @JsonProperty("p_name")
    private String pname;
    private Integer ecommerce;
    private String googleToken;
    private Integer currencyIso;
    private YeshteryState yeshteryState;
}
