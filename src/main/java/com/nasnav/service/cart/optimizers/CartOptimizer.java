package com.nasnav.service.cart.optimizers;

import java.util.Optional;

public interface CartOptimizer<T> {
	Optional<OptimizedCart> createOptimizedCart(Optional<T> parameters);
	Class<? extends T> getParameterClass();
}
