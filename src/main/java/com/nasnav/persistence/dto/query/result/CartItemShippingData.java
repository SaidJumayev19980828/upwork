package com.nasnav.persistence.dto.query.result;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemShippingData {
	private Long stockId;
	private Long shopId;
	private Long shopAddressId;
}
