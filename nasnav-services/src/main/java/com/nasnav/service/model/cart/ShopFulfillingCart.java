package com.nasnav.service.model.cart;

import com.nasnav.persistence.dto.query.result.CartItemStock;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShopFulfillingCart {
	private Long shopId;
	private Long shopCityId;
	private List<CartItemStock> cartItems;
}
