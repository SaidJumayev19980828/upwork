package com.nasnav.service.cart.optimizers.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WarehouseOptimizerCommonParameters {
	@JsonProperty("warehouse_id")
	private Long warehouseId;
}
