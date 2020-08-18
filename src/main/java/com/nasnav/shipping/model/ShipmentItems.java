package com.nasnav.shipping.model;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

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
	
	public ShipmentItems(Long stockId) {
		this.stockId = stockId;
	}
}
