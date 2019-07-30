package com.nasnav.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper=false)
public class ProductRepresentationObject extends BaseRepresentationObject{

    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Boolean available;
    private Long categoryId;
    private Long brandId;
    private String barcode;
    @JsonProperty("p_name")
    private String  pname;
}
