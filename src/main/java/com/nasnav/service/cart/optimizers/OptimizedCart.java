package com.nasnav.service.cart.optimizers;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptimizedCart {
	private List<OptimizedCartItem> cartItems;
}
