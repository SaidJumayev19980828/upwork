package com.nasnav.shipping.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ShippingDetails {
	private ShippingAddress source;
	private ShippingAddress destination;
	private ShipmentReceiver receiver;
	private Map<String,String> additionalData;
	private Map<String,String> serviceParameters;
	private List<ShipmentItems> items;
	private Long subOrderId;
	private Long metaOrderId;
	private Long returnRequestId;
	private String callBackUrl;
	private Long shopId;
	private BigDecimal codValue;
	private String paymentMethodId;
	private String shippingServiceId;
}
