package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BasketItem {

    private Long id;
    @JsonIgnore
    private Long orderId;
    private Long productId;
    private String name;
    @JsonProperty("p_name")
    private String pname;
    private Integer productType;
    private Long stockId;
    private Long brandId;
    private Map<String, String> variantFeatures;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String unit;
    private String thumb;
    private BigDecimal price;
    private BigDecimal discount;
    private Long variantId;
    private String variantName;
    private Boolean isReturnable;
    private String currencyValue;
    private String sku;
    private String productCode;
    private String currency;
    @JsonIgnore
    private Integer availableStock;
}
