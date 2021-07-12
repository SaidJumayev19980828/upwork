package com.nasnav.shipping.model;

import com.nasnav.service.model.common.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShippingServiceInfo {
	private String id;
	private String name;
	private boolean publicService;
	private List<Parameter> serviceParams;
	private List<Parameter> additionalDataParams;
	private ShippingServiceType type;
	private String icon;
}
