package com.nasnav.dto.request.cart;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CartOptimizeDTO {
	private String strategy;
	
	@JsonProperty("parameters")
	private Map parametersJson;
}
