package com.nasnav.shipping.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShippingServiceInfo {
	private String id;
	private String name;
	private boolean publicService;
	private List<Parameter> serviceParams;
	private List<Parameter> additionalDataParams; 
}
