package com.nasnav.service.cart.optimizers;

import java.util.Optional;

public interface CartOptimizer<T> {
	//TODO: i think it is better to make the optimized take the cart from upper layers instead of fetching it.
	Optional<OptimizedCart> createOptimizedCart(Optional<T> parameters);
	Class<? extends T> getParameterClass();
}
