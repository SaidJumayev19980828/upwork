package com.nasnav.dto.request.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CartOptimizationSetttingDTO {
	private String strategyName;
	private String parameters;
}
