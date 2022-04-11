package com.nasnav.payments.paymob;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShippingDetails {
    private String notes = "";
    private Long numberOfPackages = 0l;
    private BigDecimal weight = BigDecimal.ZERO;
    private String weightUnit = "";
    private BigDecimal length = BigDecimal.ZERO;
    private BigDecimal width = BigDecimal.ZERO;
    private BigDecimal height = BigDecimal.ZERO;
    private String contents = "";

}
