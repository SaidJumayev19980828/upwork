package com.nasnav.service.cart.optimizers;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OptimizedCart {
	private List<OptimizedCartItem> cartItems;
}
