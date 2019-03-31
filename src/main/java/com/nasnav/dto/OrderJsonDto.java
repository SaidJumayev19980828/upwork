package com.nasnav.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "User's order")
public class OrderJsonDto{

	@ApiModelProperty(value = "Order ID. If empty: new order", example = "12345")
	@JsonProperty("order_id")
	private Long id;

	@ApiModelProperty(value = "Order status. Default = NEW", example = "CLIENT_CONFIRMED")
	@JsonProperty("status")
	private String status;

	@ApiModelProperty(value = "User's basket items", required = true)
	@JsonProperty("basket")
	private Object[] basket;

	@ApiModelProperty(value = "Delivery address", example = "Somewhere behind a grocery store")
	@JsonProperty("delivery_address")
	private String address;
}
