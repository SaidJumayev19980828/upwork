package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTracker {
	private String shipmentExternalId;
	private String tracker;
	private String airwayBillFile;
	private ShippingDetails shippingDetails;

	public ShipmentTracker(ShipmentTracker original, String airwayBillFile){
		this.shipmentExternalId = original.getShipmentExternalId();
		this.tracker = original.getTracker();
		this.shippingDetails = original.getShippingDetails();
		this.airwayBillFile = airwayBillFile;
	}


	public ShipmentTracker(ShippingDetails shippingDetails){
		this.shippingDetails = shippingDetails;
	}
}
