package com.nasnav.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MetaOrderBasicInfo {
    private Long id;
    private LocalDateTime createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer statusInt;
    private BigDecimal grandTotal;
    private String operator;
    private String shippingServiceId;
    private String status;

    public MetaOrderBasicInfo(Long id, LocalDateTime createdAt, Integer status, BigDecimal grandTotal,
                              String operator, String shippingServiceId) {
        this.id = id;
        this.createdAt = createdAt;
        this.statusInt = status;
        this.grandTotal = grandTotal;
        this.operator = operator;
        this.shippingServiceId = shippingServiceId;
    }
}
