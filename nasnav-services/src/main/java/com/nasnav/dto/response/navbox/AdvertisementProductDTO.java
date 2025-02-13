package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.response.BrandsDTO;
import com.nasnav.dto.response.RuleResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AdvertisementProductDTO {
    private Long id;
    private Long productId;
    private ProductDetailsDTO productDetailsDTO;
    private BrandsDTO brandsDTO;
    private Integer coins;
    private Integer likes;
    private Set<Long> compensationRules;
    private Set<RuleResponseDTO> rules;

}
