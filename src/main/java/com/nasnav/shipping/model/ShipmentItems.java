package com.nasnav.shipping.model;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShipmentItems {
	private Long stockId;
	private String notes;
	private BigDecimal weight;
	
	public ShipmentItems(Long stockId) {
		this.stockId = stockId;
	}
}
