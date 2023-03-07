package com.nasnav.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartItemAddonDetailsDTO {
	
	private Long addonStockId;
	private Long addonItemId;
	private String addoneName;
	private BigDecimal price;

}
