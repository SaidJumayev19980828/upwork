package com.nasnav.service.cart.optimizers.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WarehouseOptimizerConfig {
	@JsonProperty("warehouse_id")
	private Long warehouseId;
	
	public WarehouseOptimizerConfig() {
		this.warehouseId = -1L;
	}
}
