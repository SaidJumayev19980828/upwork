package com.nasnav.dto.request.shipping;

import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShippingServiceRegistration {
	private String serviceId;
	private Map<String,Object> serviceParameters;
}
