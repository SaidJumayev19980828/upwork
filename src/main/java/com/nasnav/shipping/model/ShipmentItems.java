package com.nasnav.shipping.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ShipmentItems {
	private Long stockId;
	private String notes;
	private BigDecimal weight;
}
