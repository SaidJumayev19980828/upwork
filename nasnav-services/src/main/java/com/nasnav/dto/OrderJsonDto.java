package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.enumerations.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(name = "User's order")
public class OrderJsonDto{

	@Schema(name = "Order ID", example = "12345")
	@JsonProperty("order_id")
	private Long id;

	@Schema(name = "Order status", example = "CLIENT_CONFIRMED")
	private OrderStatus status;
}
