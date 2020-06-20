package com.nasnav.shipping.model;

import lombok.Data;

@Data
public class ShipmentTracker {
	private String shipmentExternalId;
	private String tracker;
	private Byte[] airwayBillFile;
}
