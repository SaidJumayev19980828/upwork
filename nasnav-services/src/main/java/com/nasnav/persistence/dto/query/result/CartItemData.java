package com.nasnav.persistence.dto.query.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemData {
	private Long id;
	private Long userId;
	private Long productId;
	private Long variantId;
	private String variantName;
	private Long stockId;
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
	private String unit;
}
