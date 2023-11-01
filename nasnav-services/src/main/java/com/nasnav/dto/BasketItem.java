package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.AddonBasketEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper=false)
public class BasketItem extends BaseRepresentationObject {

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
    private String brandName;
    private String brandLogo;
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
    private BigDecimal addonTotal;
    @JsonIgnore
    private Integer availableStock;
    private List<AddonDetailsDTO> addons;
   	private String specialOrder;
    @JsonIgnore
    private Map<String, String> variantFeature;
    @JsonAnyGetter
    public Map<String,String> getVariantFeatureMap() {
        return variantFeature;
    }

}
