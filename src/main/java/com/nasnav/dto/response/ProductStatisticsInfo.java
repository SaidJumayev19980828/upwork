package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
public class ProductStatisticsInfo {
    private Long productId;
    private Long variantId;
    private String variantName;
    private String barcode;
    private String sku;
    private String productCode;
    private Long quantity;
    private BigDecimal total;
    @JsonIgnore
    private Date date;
}
