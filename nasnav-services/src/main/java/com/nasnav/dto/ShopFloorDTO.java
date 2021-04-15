package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class ShopFloorDTO extends BaseRepresentationObject{

    private Long id;

    private Integer number;

    private String name;

    @JsonProperty("shop_sections")
    private List<ShopSectionsDTO> shopSections;
}
