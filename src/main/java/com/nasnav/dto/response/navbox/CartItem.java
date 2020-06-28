package com.nasnav.dto.response.navbox;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CartItem {
	private Long id;
	private Long productId;
	private Long variantId;
	private Long stockId;
	private Map<String,String> variantFeatures;
	private String coverImg;
	private BigDecimal price;
	private Integer quantity;
	private String brandName;
	private Long brandId;
	private String brandLogo;
	private String name;
}
