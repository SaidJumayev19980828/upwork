package com.nasnav.shipping.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ShippingDetails {
	private ShippingAddress source;
	private ShippingAddress destination;
	private ShipmentReceiver receiver;
	private Map<String,String> additionalData;
	private Map<String,String> serviceParameters;
	private List<ShipmentItems> items;
	private Long subOrderId;
	private String callBackUrl;
	private Long shopId;
}
