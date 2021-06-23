package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class ReturnRequestItemDTO extends BaseRepresentationObject {
    private Long id;
    private Integer returnedQuantity;
    private Integer receivedQuantity;
    private Long receivedBy;
    private LocalDateTime receivedOn;
    private Long createdByUser;
    private Long createdByEmployee;
    private Long basketItem;
    private Long shopId;
    private String shopName;
    private Long productId;
    private Long subOrderId;
    private String productName;
    private String productCode;
    private String sku;
    private Long variantId;
    private Map<String, String> variantFeatures;
    private BigDecimal price;
    private String coverImage;
    private AddressRepObj address;
}
