package com.nasnav.service.cart.optimizers;

import java.util.Optional;

import com.nasnav.dto.response.navbox.Cart;

public interface CartOptimizer<T> {
	Optional<Cart> createOptimizedCart(Optional<T> parameters);
	Class<? extends T> getParameterClass();
}
