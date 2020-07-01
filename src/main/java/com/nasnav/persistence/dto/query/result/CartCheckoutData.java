package com.nasnav.persistence.dto.query.result;

import com.nasnav.persistence.AddressesEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutData {
    private Long id;
    private Long stockId;
    private Integer currency;
    private BigDecimal price;
    private Integer quantity;
    private String variantBarcode;
    private String productName;
    private String featureSpec;
    private Long shopId;
    private AddressesEntity shopAddress;
    private Long organizationId;
}
