package com.nasnav.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.enumerations.OrderStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "User's order")
public class OrderJsonDto{

	@ApiModelProperty(value = "Order ID", example = "12345")
	@JsonProperty("order_id")
	private Long id;

	@ApiModelProperty(value = "Order status", example = "CLIENT_CONFIRMED")
	private OrderStatus status;
}
