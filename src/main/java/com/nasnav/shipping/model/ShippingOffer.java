package com.nasnav.shipping.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShippingOffer {
	private ShippingServiceInfo service;
	private List<Shipment> shipments;
}
