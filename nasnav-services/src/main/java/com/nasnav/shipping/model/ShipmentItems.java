package com.nasnav.shipping.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ShipmentItems {
	private Long stockId;
	private String barcode;
	private Integer quantity;
	private String name;
	private String specs;
	private String notes;
	private BigDecimal weight;
	private String productCode;
	private String sku;
	private Long returnedItemId;
	private BigDecimal price;
	
	public ShipmentItems(Long stockId) {
		this.stockId = stockId;
	}
}
