package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class StatisticsCartItem {
    private Long id;
    private Long productId;
    private Long variantId;
    private String variantName;
    private Long stockId;
    private String coverImg;
    private BigDecimal price;
    private Integer quantity;
    private String name;
    private String barcode;
    private BigDecimal discount;
    private LocalDateTime createdAt;
}
