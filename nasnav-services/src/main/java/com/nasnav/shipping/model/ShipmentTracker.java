package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.nasnav.shipping.model.Constants.DEFAULT_AWB_FILE_MIME;
import static com.nasnav.shipping.model.Constants.DEFAULT_AWB_FILE_NAME;
import static java.util.Optional.ofNullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTracker {
	private String shipmentExternalId;
	private String tracker;
	private String airwayBillFile;
	private ShippingDetails shippingDetails;
	private String airwayBillFileName;
	private String airwayBillFileMime;

	public ShipmentTracker(ShipmentTracker original, String airwayBillFile){
		this.shipmentExternalId = original.getShipmentExternalId();
		this.tracker = original.getTracker();
		this.shippingDetails = original.getShippingDetails();
		this.airwayBillFileName = original.getAirwayBillFileName();
		this.airwayBillFileMime = original.getAirwayBillFileMime();
		this.airwayBillFile = airwayBillFile;
	}


	public ShipmentTracker(String shipmentExternalId, String tracker, String airwayBillFile, ShippingDetails shippingDetails) {
		this.shipmentExternalId = shipmentExternalId;
		this.tracker = tracker;
		this.airwayBillFile = airwayBillFile;
		this.shippingDetails = shippingDetails;
	}

	public ShipmentTracker(ShippingDetails shippingDetails){
		this.shippingDetails = shippingDetails;
	}
}
