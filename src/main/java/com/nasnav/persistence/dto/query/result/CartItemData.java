package com.nasnav.persistence.dto.query.result;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemData {
	private Long id;
	private Long userId;
	private Long productId;
	private Long variantId;
	private String variantName;
	private Long stockId;
	private String featureSpec;
	private BigDecimal weight;
	private String coverImg;
	private BigDecimal price;
	private Integer quantity;	
	private Long brandId;
	private String brandName;
	private String brandLogo;
	private String productName;
	private Integer productType;
	private BigDecimal discount;
	private String additionalData;
}
