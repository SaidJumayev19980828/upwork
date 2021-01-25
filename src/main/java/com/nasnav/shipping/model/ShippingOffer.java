package com.nasnav.shipping.model;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.Collections.emptyList;

@Data
@NoArgsConstructor
public class ShippingOffer {
	private ShippingServiceInfo service;
	private List<Shipment> shipments;
	private boolean available;
	private String message;


	public ShippingOffer(ShippingServiceInfo service, List<Shipment> shipments){
		this.service = service;
		this.shipments = shipments;
		this.available = true;
	}


	public ShippingOffer(ShippingServiceInfo service, String message){
		this.available = false;
		this.service = service;
		this.message = message;
		this.shipments = emptyList();
	}
}
