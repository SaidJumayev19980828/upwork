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

	@ApiModelProperty(value = "Order ID. If empty: new order", example = "12345")
	@JsonProperty("order_id")
	private Long id;

	@ApiModelProperty(value = "Order status. DefaultDTO = NEW", example = "CLIENT_CONFIRMED")
	@JsonProperty("status")
	private String status;

	@ApiModelProperty(value = "User's basket items", required = true)
	@JsonProperty("basket")
	private List<BasketItemDTO> basket;

	@ApiModelProperty(value = "Delivery address id", example = "123")
	@JsonProperty("address_id")
	private Long addressId;
	
	
	
	public OrderJsonDto(){
		status = OrderStatus.NEW.toString();
		basket = new ArrayList<>();
	}
}
