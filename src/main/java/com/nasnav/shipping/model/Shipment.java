package com.nasnav.shipping.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Shipment {
	private BigDecimal shippingFee;
	private ShippingEta eta;
	private List<Long> stocks;
}
