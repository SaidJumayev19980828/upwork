package com.nasnav.service.cart.optimizers.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SameCityCartOptimizerParameters {
	@JsonProperty("CUSTOMER_ADDRESS_ID")
	private Long customerAddressId;
	
	@JsonProperty("SHOP_ID")
	private Long shopId;
}
