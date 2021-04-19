package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class Shipment {
	private BigDecimal shippingFee;
	private ShippingEta eta;
	private List<Long> stocks;
	private Long subOrderId;
}
