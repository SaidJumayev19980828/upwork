package com.nasnav.service.model.cart;

import java.util.List;

import com.nasnav.persistence.dto.query.result.CartItemStock;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShopFulfillingCart {
	private Long shopId;
	private Long shopCityId;
	private List<CartItemStock> cartItems;
}
