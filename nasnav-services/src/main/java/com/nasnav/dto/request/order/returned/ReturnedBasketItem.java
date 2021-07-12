package com.nasnav.dto.request.order.returned;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReturnedBasketItem {
	private Long orderItemId;
	private Integer returnedQuantity;
}
