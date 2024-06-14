package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.response.ThreeDModelResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper=true)
@JsonPropertyOrder({ "image_url", "categoryId", "price" })
public class ProductRepresentationObject extends ProductBaseInfo{
    
    private BigDecimal price;
    private Prices prices;
    private Long brandId;
    private Long categoryId;
    private String barcode;
    private String description;
    private BigDecimal discount;
    private int currency;
    private Long stockId;
    private boolean multipleVariants;
    private boolean hidden;
    private Map<String, String> defaultVariantFeatures;
    private List<TagsRepresentationObject> tags;
    private String creationDate;
    private String updateDate;
    private Long quantity;
    private Boolean has_360_view;
    protected Integer productType;
    @JsonProperty("shop_360s")
    private List<Long> shops;
    private String productCode;
    private String sku;
    private Long organizationId;
    private List<ProductImageDTO> images;
    private Double rating;
    private Integer priority;
    private Long id;
    private List<ProductPromotionDto> promotions;
    private Long modelId;
    @JsonProperty("3dModel")
    private ThreeDModelResponse threeDModel;


    public ProductRepresentationObject() {
        images = new ArrayList<>();
        tags = new ArrayList<>();
        shops = new ArrayList<>();
    }
}
