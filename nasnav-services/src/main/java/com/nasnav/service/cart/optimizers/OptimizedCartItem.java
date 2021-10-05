package com.nasnav.service.cart.optimizers;

import com.nasnav.dto.response.navbox.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptimizedCartItem {
	private CartItem cartItem;
	private Boolean priceChanged;
	private Boolean itemChanged;
}