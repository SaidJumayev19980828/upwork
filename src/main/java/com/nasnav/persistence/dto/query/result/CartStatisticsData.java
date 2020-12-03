package com.nasnav.persistence.dto.query.result;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CartStatisticsData {
    private Long variantId;
    private String variantName;
    private String barcode;
    private String variantCode;
    private String sku;
    private Long quantity;
    private Long usersCount;
}
