package com.nasnav.dto.request.organization;

import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CartOptimizationSettingDTO {
	private String strategyName;
	private String shippingServiceId;
	private Map<String,Object> parameters;
}
