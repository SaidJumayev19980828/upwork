package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.enumerations.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class OrderJsonDto{
	@JsonProperty("order_id")
	private Long id;
	private OrderStatus status;
}
