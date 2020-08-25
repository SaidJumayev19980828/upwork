package com.nasnav.service.cart.optimizers;

import java.util.Optional;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;

public interface CartOptimizer<CartParams, CommonParams> {
	Optional<CartParams> createCartOptimizationParameters(CartCheckoutDTO dto);
	Optional<OptimizedCart> createOptimizedCart(Optional<CartParams> parameters, Cart cart );
	Class<? extends CartParams> getCartParametersClass();
	Class<? extends CommonParams> getCommonParametersClass();
	Boolean areCommonParametersValid(CommonParams parameters);
	Boolean areCartParametersValid(CartParams parameters);
}
