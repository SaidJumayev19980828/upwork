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
}
