package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
public class PickupItem {
    private Long id;
    private Long productId;
    private Long variantId;
    private String variantName;
    private Long stockId;
    private Map<String,String> variantFeatures;
    private String coverImg;
    private BigDecimal price;
    private Integer quantity;
    private String brandName;
    private Long brandId;
    private String brandLogo;
    private String name;
    private Integer productType;
    private BigDecimal discount;
    private Map<String,Object> additionalData;
    private Long userId;
    private BigDecimal weight;
    private String unit;
    private Long orgId;
}
